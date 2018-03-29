package com.hsiaosiyuan.jexpose;

import com.hsiaosiyuan.jexpose.signature.Parser;
import com.hsiaosiyuan.jexpose.signature.node.ClassSignature;
import com.hsiaosiyuan.jexpose.signature.node.MethodTypeSignature;
import com.hsiaosiyuan.jexpose.signature.node.TypeSignature;
import org.objectweb.asm.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClassResolver extends ClassVisitor {

  private static final HashMap<String, ClassSignature> classPool = new HashMap<>();

  private String jrt;
  private String jar;
  private String binaryName;

  private ClassSignature clazz;
  private CompletableFuture<ClassSignature> result;

  ClassResolver(String jrt, String jar, String name) {
    super(Opcodes.ASM6);
    this.jrt = jrt;
    this.jar = jar;
    this.binaryName = name.replace(".", "/");
  }

  Future<ClassSignature> resolve() throws IOException {
    clazz = classPool.get(binaryName);
    if (clazz != null) {
      return CompletableFuture.completedFuture(clazz);
    }

    // if class is `builtin` just skip load it's content
    // doing this to minify the size of classPool
    if (isBuiltin(binaryName)) {
      clazz = new ClassSignature();
      clazz.binaryName = binaryName;
      classPool.put(binaryName, clazz);
      return CompletableFuture.completedFuture(clazz);
    }

    byte[] raw = loadClazzBytes(binaryName);
    result = new CompletableFuture<>();

    new ClassReader(raw).accept(this, ClassReader.SKIP_DEBUG);
    return result;
  }

  private byte[] loadClazzBytes(String binaryName) throws FileNotFoundException {
    String f = binaryName + ".class";
    try {
      Path path = Paths.get(jar, f);
      return Files.readAllBytes(path);
    } catch (IOException ignored) {
    }

    Path path = Paths.get(jrt, f);
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new FileNotFoundException("unable to load class via: " + path.toString());
    }
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);

    if (signature != null) {
      clazz = new Parser(signature).parseClassSignature();
    } else if (superName != null) {
      clazz = new ClassSignature();
      try {
        ClassSignature n = new ClassResolver(jrt, jar, superName).resolve().get();
        clazz.superClasses.add(n.toClassTypeSignature());
      } catch (InterruptedException | ExecutionException | IOException e) {
        e.printStackTrace();
      }
    } else {
      clazz = new ClassSignature();
    }
    clazz.setName(name);
    clazz.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
    clazz.isEnum = (access & Opcodes.ACC_ENUM) != 0;

    classPool.put(clazz.binaryName, clazz);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    TypeSignature ts = new Parser(signature != null ? signature : descriptor).parseTypeSignature();
    ts.isStatic = (access & Opcodes.ACC_STATIC) != 0;
    clazz.fields.put(name, ts);
    return super.visitField(access, name, descriptor, signature, value);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    MethodTypeSignature ts = new Parser(signature != null ? signature : descriptor).parseMethodTypeSignature();
    clazz.addMethod(name, ts);
    super.visitMethod(access, name, descriptor, signature, exceptions);
    return new MethodVisitor(Opcodes.ASM6) {
      @Override
      public void visitParameter(String name, int access) {
        ts.formalParams.add(name);
      }
    };
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
    HashSet<String> refs = clazz.collectRefClasses();
    for (String ref : refs) {
      try {
        new ClassResolver(jrt, jar, ref).resolve().get();
      } catch (InterruptedException | ExecutionException | IOException e) {
        e.printStackTrace();
      }
    }
    if (result != null) {
      result.complete(clazz);
    }
  }

  public static HashMap<String, ClassSignature> getClassPool() {
    return classPool;
  }

  public static HashMap<String, ClassSignature> getClassPoolWithoutBuiltin() {
    HashMap<String, ClassSignature> ret = new HashMap<>();
    for (Map.Entry<String, ClassSignature> entry : classPool.entrySet()) {
      String key = entry.getKey();
      if (isBuiltin(key)) {
        continue;
      }
      ret.put(key, entry.getValue());
    }
    return ret;
  }

  private static boolean isBuiltin(String binaryName) {
    return binaryName.startsWith("java/") || binaryName.startsWith("sun/");
  }
}
