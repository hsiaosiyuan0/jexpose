package com.hsiaosiyuan.jexpose;

import com.hsiaosiyuan.jexpose.signature.Parser;
import com.hsiaosiyuan.jexpose.signature.node.ClassTypeSignature;
import com.hsiaosiyuan.jexpose.signature.node.MethodTypeSignature;
import com.hsiaosiyuan.jexpose.signature.node.TypeArg;
import com.hsiaosiyuan.jexpose.signature.node.TypeSignature;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TypeResolver extends ClassVisitor {

  private static final HashMap<String, Type> typesPool = new HashMap<>();

  private String jrt;
  private String jar;
  private String name;
  private boolean isInterface;
  private Type type;
  private CompletableFuture<Type> result;

  TypeResolver(String jrt, String jar, String name, boolean isInterface) {
    super(Opcodes.ASM6);
    this.jrt = jrt;
    this.jar = jar;
    this.name = name;
    this.isInterface = isInterface;
  }

  Future<Type> resolve() throws IOException {
    Type type = typesPool.get(name);
    if (type != null) {
      return CompletableFuture.completedFuture(type);
    }

    byte[] raw = loadClazzBytes(name);
    result = new CompletableFuture<>();

    new ClassReader(raw).accept(this, ClassReader.SKIP_DEBUG);
    return result;
  }

  private byte[] loadClazzBytes(String name) throws FileNotFoundException {
    String f = name.replace(".", File.separator) + ".class";
    try {
      Path path = Paths.get(jar, f);
      return Files.readAllBytes(path);
    } catch (IOException ignored) {
    }

    Path path = Paths.get(jrt, f);
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new FileNotFoundException("unable to load class via: " +
        path.toString());
    }
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);

    if (isInterface) {
      type = Type.newInterface(name);
    } else {
      type = Type.newClazz(name);
    }
    typesPool.put(type.name, type);

    try {
      if (superName != null) {
        type.superClazz =
          new TypeResolver(jrt, jar, superName, false).resolve().get();
      }

      for (String itf : interfaces) {
        Type t = new TypeResolver(jrt, jar, itf, true).resolve().get();
        type.interfaces.add(t);
      }
    } catch (InterruptedException | ExecutionException | IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    TypeSignature ts = new Parser(signature != null ? signature : descriptor).parseTypeSignature();
    try {
      type.fields.put(name, resolveTypeSignature(ts));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.visitField(access, name, descriptor, signature, value);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    Type m = Type.newMethod(name);
    MethodTypeSignature ts = new Parser(signature != null ? signature : descriptor).parseMethodTypeSignature();
    try {
      MethodSignature ms = resolveMethodTypeSignature(ts);
      m.params.addAll(ms.params);
      m.ret = ms.ret;
    } catch (Exception e) {
      e.printStackTrace();
    }
    type.methods.put(name, m);
    return super.visitMethod(access, name, descriptor, signature, exceptions);
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
    if (result != null && type != null) {
      result.complete(type);
    }
  }

  private Type resolveTypeSignature(TypeSignature ts) throws Exception {
    if (ts.isPrimitive()) {
      Type t = Type.newPrimitive(ts.asPrimitive().name);
      return recursiveResolve(t);
    }

    if (ts.isClass()) {
      ClassTypeSignature cls = ts.asClass();
      Type t = Type.newClazz(cls.getFullQualifiedName());
      if (cls.hasTypeArgs()) {
        for (TypeArg ta : cls.typeArgs) {
          if (ta.isWildcard) {
            t.typeArgs.add(Type.newWildcardType());
            continue;
          }
          if (ta.type.isTypeVar()) {
            t.typeArgs.add(Type.newTypeVar(ta.type.asTypeVar().name));
          } else {
            t.typeArgs.add(resolveTypeSignature(ta.type));
          }
        }
      }
      return recursiveResolve(t);
    }

    if (ts.isArray()) {
      Type t = Type.newArray();
      t.arrVal = resolveTypeSignature(ts.asArray().elementType);
      return t;
    }

    if (ts.isTypeVar()) {
      return Type.newTypeVar(ts.asTypeVar().name);
    }

    if (ts.isVoidType()) {
      return Type.newVoidType();
    }

    throw new Exception("unreachable");
  }

  private MethodSignature resolveMethodTypeSignature(MethodTypeSignature ts) throws Exception {
    MethodSignature s = new MethodSignature();
    if (ts.hasParams()) {
      for (TypeSignature p : ts.params) {
        s.params.add(resolveTypeSignature(p));
      }
    }
    s.ret = resolveTypeSignature(ts.ret);
    return s;
  }

  private Type recursiveResolve(Type type) throws IOException, ExecutionException, InterruptedException {
    Type t = type;
    if (type.isClazzOrInterface()) {
      t = new TypeResolver(jrt, jar, type.name, false).resolve().get();
    }

    if (type.isGeneric()) {
      LinkedList<Type> args = new LinkedList<>();
      for (Type ta : type.typeArgs) {
        if (ta.isClazzOrInterface() || ta.isArray()) {
          args.add(recursiveResolve(ta));
        } else {
          args.add(ta);
        }
      }
      t.typeArgs = args;
    }

    if (type.isArray()) {
      t.arrVal = recursiveResolve(type.arrVal);
    }

    return t;
  }

  static class MethodSignature {
    Type ret;
    ArrayList<Type> params = new ArrayList<>();
  }
}
