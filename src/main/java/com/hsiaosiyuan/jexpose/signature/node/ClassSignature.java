package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;
import com.hsiaosiyuan.jexpose.ClassResolver;
import com.hsiaosiyuan.jexpose.signature.Parser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.stream.Collectors;

public class ClassSignature extends Node {
  @JSONField(serialize = false)
  public String binaryName;

  @JSONField(serialize = false)
  public ArrayList<TypeParam> typeParams;

  @JSONField(serialize = false)
  public ArrayList<TypeArg> appliedTypeArgs;

  @JSONField(serialize = false)
  public ArrayList<ClassTypeSignature> superClasses;

  public boolean isInterface;

  public boolean isEnum;

  @JSONField(serialize = false)
  public String jar;
  @JSONField(serialize = false)
  public String jrt;

  @JSONField(serialize = false)
  public HashMap<String, TypeSignature> fields;

  public ArrayList<Object> values;

  @JSONField(serialize = false)
  public HashMap<String, MethodTypeSignature> methods;

  public ClassSignature() {
    typeParams = new ArrayList<>();
    superClasses = new ArrayList<>();
    fields = new HashMap<>();
    values = new ArrayList<>();
    methods = new HashMap<>();
  }

  public void setName(String name) {
    binaryName = name.replace(".", "/");
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      for (FieldTypeSignature ts : tp.types) {
        refs.addAll(ts.collectRefClasses());
      }
    }
    for (ClassTypeSignature cs : superClasses) {
      refs.addAll(cs.collectRefClasses());
    }
    for (TypeSignature ts : fields.values()) {
      refs.addAll(ts.collectRefClasses());
    }
    for (MethodTypeSignature ms : methods.values()) {
      refs.addAll(ms.collectRefClasses());
    }
    return refs;
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      for (FieldTypeSignature ts : tp.types) {
        refs.addAll(ts.getDirectRefClasses());
      }
    }
    try {
      for (TypeSignature ts : getFinalFields().values()) {
        refs.addAll(ts.getDirectRefClasses());
      }
    } catch (Exception ignored) {
    }
    for (MethodTypeSignature ms : methods.values()) {
      refs.addAll(ms.getDirectRefClasses());
    }
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  public ClassTypeSignature toClassTypeSignature() {
    ClassTypeSignature node = new ClassTypeSignature();
    node.binaryName = binaryName;
    return node;
  }

  @JSONField(serialize = false)
  public ClassTypeSignature getSuperClass() {
    if (superClasses.size() > 0) return superClasses.get(0);
    return null;
  }

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }

  public static ArrayList<TypeArg> typeParams2typeArgs(ArrayList<TypeParam> params) {
    ArrayList<TypeArg> ret = new ArrayList<>();
    for (TypeParam p : params) {
      ret.add(p.toTypeArg());
    }
    return ret;
  }

  @JSONField(name = "fields")
  public HashMap<String, TypeSignature> getFinalFields() throws Exception {
    HashMap<String, ClassSignature> classPool = ClassResolver.getClassPool();
    HashMap<String, TypeSignature> fields = new HashMap<>();
    ArrayList<ClassTypeSignature> superClasses = new ArrayList<>();
    ClassTypeSignature sct = getSuperClass();
    while (sct != null) {
      superClasses.add(sct);
      sct = classPool.get(sct.binaryName).getSuperClass();
    }

    ArrayList<HashMap<String, TypeSignature>> superFields = new ArrayList<>();
    ArrayList<TypeParam> prevTypeParams = typeParams;
    ArrayList<TypeArg> prevAppliedTypeArgs = typeParams2typeArgs(typeParams);
    for (ClassTypeSignature sp : superClasses) {
      ArrayList<TypeArg> args = new ArrayList<>();

      for (TypeArg ta : sp.typeArgs) {
        if (ta.type.isTypeVar()) {
          int idx = getTypeParamIndex(prevTypeParams, ta.type.asTypeVar().name);
          try {
            TypeArg tp = prevAppliedTypeArgs.get(idx);
            args.add(tp);
          } catch (Exception ignored) {
          }
        } else {
          args.add(ta);
        }
      }

      ClassSignature spc = ClassResolver.getClassPool().get(sp.binaryName);
      HashMap<String, TypeSignature> appliedFields = spc.applyTypeArgs(args);
      superFields.add(appliedFields);
      prevTypeParams = spc.typeParams;
      prevAppliedTypeArgs = spc.appliedTypeArgs;
    }

    for (int i = superFields.size() - 1; i >= 0; --i) {
      fields.putAll(superFields.get(i));
    }

    fields.putAll(this.fields);
    if (isEnum) {
      fields = (HashMap<String, TypeSignature>) fields.entrySet().stream()
        .filter(item -> !item.getKey().equals("$VALUES"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      extractEnumValues();
      return fields;
    }

    HashMap<String, TypeSignature> noStatic = new HashMap<>();
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      String key = entry.getKey();
      TypeSignature value = entry.getValue();
      if (!value.isStatic) {
        noStatic.put(key, value);
      }
    }
    return noStatic;
  }

  @JSONField(name = "methods")
  public HashMap<String, MethodTypeSignature> getMethods() {
    HashMap<String, MethodTypeSignature> noInternal = new HashMap<>();
    if (isEnum) return noInternal;

    for (Map.Entry<String, MethodTypeSignature> entry : methods.entrySet()) {
      String key = entry.getKey();
      MethodTypeSignature value = entry.getValue();
      if (key.startsWith("<init>") || key.startsWith("<clinit>")) {
        continue;
      }
      noInternal.put(key, value);
    }
    return noInternal;
  }

  @JSONField(serialize = false)
  public static int getTypeParamIndex(ArrayList<TypeParam> params, String name) {
    for (int i = 0; i < params.size(); ++i) {
      if (params.get(i).name.equals(name)) return i;
    }
    return -1;
  }

  public HashMap<String, TypeSignature> copyFields() throws CloneNotSupportedException {
    HashMap<String, TypeSignature> ret = new HashMap<>();
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      ret.put(entry.getKey(), (TypeSignature) entry.getValue().clone());
    }
    return ret;
  }

  public HashMap<String, TypeSignature> applyTypeArgs(ArrayList<TypeArg> args) throws CloneNotSupportedException {
    appliedTypeArgs = new ArrayList<>();
    HashMap<String, TypeSignature> fields = copyFields();
    LinkedHashMap<String, TypeArg> namedArgs = new LinkedHashMap<>();
    if (args.size() == 0) {
      for (TypeParam tp : typeParams) {
        TypeArg ta = new TypeArg();
        ta.type = tp.types.get(0);
        namedArgs.put(tp.name, ta);
      }
    } else {
      for (int i = 0; i < typeParams.size(); ++i) {
        namedArgs.put(typeParams.get(i).name, args.get(i));
      }
    }
    for (Map.Entry<String, TypeSignature> entry : fields.entrySet()) {
      String key = entry.getKey();
      TypeSignature val = entry.getValue();
      if (val.isTypeVar()) {
        fields.put(key, namedArgs.get(val.asTypeVar().name).type);
      } else {
        val.applyTypeArgs(namedArgs);
      }
    }
    appliedTypeArgs.addAll(namedArgs.values());
    return fields;
  }

  @JSONField(name = "typeParams")
  public ArrayList<TypeParam> getFinalTypeParams() {
    return typeParams;
  }

  @JSONField(serialize = false)
  public String genUniqueMethodName(String name) {
    Set<String> names = methods.keySet();
    for (int i = 0; i < 100; ++i) {
      String n = name + "@override" + i;
      if (!names.contains(n)) return n;
    }
    throw new UnsupportedOperationException("too many override methods");
  }

  public void addMethod(String name, MethodTypeSignature method) {
    if (methods.get(name) == null) {
      methods.put(name, method);
      return;
    }
    method.isOverride = true;
    name = genUniqueMethodName(name);
    methods.put(name, method);
  }

  public void extractEnumValues() throws Exception {
    byte[] raw = ClassResolver.loadClazzBytes(jar, jrt, binaryName);
    ClassNode node = new ClassNode();
    new ClassReader(raw).accept(node, ClassReader.SKIP_DEBUG);

    @SuppressWarnings("unchecked") final List<MethodNode> methods = node.methods;
    MethodNode clinit = null;
    MethodNode init = null;
    for (MethodNode m : methods) {
      if (m.name.equals("<clinit>")) {
        clinit = m;
      } else if (m.name.equals("<init>")) {
        init = m;
      }
      if (clinit != null && init != null) break;
    }
    if (clinit == null || init == null) return;

    MethodTypeSignature mts = new Parser(init.signature).parseMethodTypeSignature();
    if (mts.params.size() == 0) return;

    if (mts.params.size() > 1) {
      System.out.println("too many params of enum constructor, max is 1, skipped: " + binaryName);
      return;
    }

    TypeSignature p0 = mts.params.get(0);
    boolean ok = false;
    if (p0.isClassType()) {
      ok = p0.asClassType().binaryName.equals("java/lang/String");
    } else if (p0.isPrimitive()) {
      ok = p0.asPrimitive().binaryName.equals("java/lang/Integer")
        || p0.asPrimitive().binaryName.equals("java/lang/Long")
        || p0.asPrimitive().binaryName.equals("java/lang/Short");
    }

    if (!ok) {
      System.out.println("unsupported type of enum constructor: " + p0);
      return;
    }

    for (int i = 0; i < clinit.instructions.size(); i++) {
      if (clinit.instructions.get(i).getOpcode() == 187) {
        // 0 new #4 <com/hsiaosiyuan/jexpose/EnumB>
        // 3 dup
        // 4 ldc #8 <B1>
        // 6 iconst_0
        // 7 ldc #9 <1>
        // 9 invokespecial #10 <com/hsiaosiyuan/jexpose/EnumB.<init>>
        i += 4;
        AbstractInsnNode insnNode = clinit.instructions.get(i);
        if (insnNode.getType() == AbstractInsnNode.LDC_INSN) {
          values.add(((LdcInsnNode) insnNode).cst);
          continue;
        }
        if (insnNode.getType() == AbstractInsnNode.INSN) {
          switch (insnNode.getOpcode()) {
            case 0x2: {
              values.add(-1);
              break;
            }
            case 0x3: {
              values.add(0);
              break;
            }
            case 0x4: {
              values.add(1);
              break;
            }
            case 0x5: {
              values.add(2);
              break;
            }
            case 0x6: {
              values.add(3);
              break;
            }
            case 0x7: {
              values.add(4);
              break;
            }
            case 0x8: {
              values.add(5);
              break;
            }
            case 0x9: {
              values.add(0);
              break;
            }
            case 0xa: {
              values.add(1);
              break;
            }
            default: {
              throw new Exception("unsupported opcode: " + insnNode.getOpcode());
            }
          }
          continue;
        }
        if (insnNode.getType() == AbstractInsnNode.INT_INSN) {
          values.add(((IntInsnNode) insnNode).operand);
        }
      }
    }
  }
}
