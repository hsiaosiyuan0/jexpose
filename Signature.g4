grammar Signature;

// reference:
// http://download.forge.objectweb.org/asm/asm4-guide.pdf
// Section: 4.1.1. Structure

// typeSignature
typeSignature
  : Primitive 
  | fieldTypeSignature
  ;

fieldTypeSignature
  : classTypeSignature
  | arrayTypeSignature
  | typeVar
  ;

arrayTypeSignature
  : '[' typeSignature
  ;

classTypeSignature
  : 'L' fullyQualifiedName typArgs? ( '.' Id typArgs? )* ';'
  ;

fullyQualifiedName
  : Id ( '/' Id )*
  ;

typArgs
  : '<' typArg+ '>'
  ;

typArg
  : '*'
  | ( '+' | '-' )? fieldTypeSignature
  ;

typeVar
  : 'T' Id ';'
  ;

Id
  : [a-zA-Z_$]+
  ;

Primitive
  : [BCDFIJSZV]
  ;

// methodTypeSignature
methodTypeSignature
  : typeParams? '(' paramList ')' returnType exception*
  ;

exception
  : '^' ( classTypeSignature | typeVar )
  ;

typeParams
  : '<' typeParam+ '>'
  ;

typeParam
  : Id ':' fieldTypeSignature? ( ':' fieldTypeSignature )*
  ;

paramList
  : typeSignature*
  ;

returnType
  : typeSignature
  | 'V'
  ;

classSignature
  : typeParams? classTypeSignature classTypeSignature*
  ;
