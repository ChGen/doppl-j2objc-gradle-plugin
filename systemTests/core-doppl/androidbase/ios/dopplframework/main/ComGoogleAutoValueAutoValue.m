//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "ComGoogleAutoValueAutoValue.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "J2ObjC_source.h"
#include "java/lang/annotation/Annotation.h"
#include "java/lang/annotation/ElementType.h"
#include "java/lang/annotation/Retention.h"
#include "java/lang/annotation/RetentionPolicy.h"
#include "java/lang/annotation/Target.h"

@interface ComGoogleAutoValueAutoValue : NSObject

@end

__attribute__((unused)) static IOSObjectArray *ComGoogleAutoValueAutoValue__Annotations$0();

@interface ComGoogleAutoValueAutoValue_Builder : NSObject

@end

__attribute__((unused)) static IOSObjectArray *ComGoogleAutoValueAutoValue_Builder__Annotations$0();

@interface ComGoogleAutoValueAutoValue_CopyAnnotations : NSObject

@end

__attribute__((unused)) static IOSObjectArray *ComGoogleAutoValueAutoValue_CopyAnnotations__Annotations$0();

@implementation ComGoogleAutoValueAutoValue

+ (const J2ObjcClassInfo *)__metadata {
  static const void *ptrTable[] = { "LComGoogleAutoValueAutoValue_Builder;LComGoogleAutoValueAutoValue_CopyAnnotations;", (void *)&ComGoogleAutoValueAutoValue__Annotations$0 };
  static const J2ObjcClassInfo _ComGoogleAutoValueAutoValue = { "AutoValue", "com.google.auto.value", ptrTable, NULL, NULL, 7, 0x2609, 0, 0, -1, 0, -1, -1, 1 };
  return &_ComGoogleAutoValueAutoValue;
}

@end

IOSObjectArray *ComGoogleAutoValueAutoValue__Annotations$0() {
  return [IOSObjectArray arrayWithObjects:(id[]){ create_JavaLangAnnotationRetention(JreLoadEnum(JavaLangAnnotationRetentionPolicy, SOURCE)), create_JavaLangAnnotationTarget([IOSObjectArray arrayWithObjects:(id[]){ JreLoadEnum(JavaLangAnnotationElementType, TYPE) } count:1 type:JavaLangAnnotationElementType_class_()]) } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleAutoValueAutoValue)

@implementation ComGoogleAutoValueAutoValue_Builder

+ (const J2ObjcClassInfo *)__metadata {
  static const void *ptrTable[] = { "LComGoogleAutoValueAutoValue;", (void *)&ComGoogleAutoValueAutoValue_Builder__Annotations$0 };
  static const J2ObjcClassInfo _ComGoogleAutoValueAutoValue_Builder = { "Builder", "com.google.auto.value", ptrTable, NULL, NULL, 7, 0x2609, 0, 0, 0, -1, -1, -1, 1 };
  return &_ComGoogleAutoValueAutoValue_Builder;
}

@end

IOSObjectArray *ComGoogleAutoValueAutoValue_Builder__Annotations$0() {
  return [IOSObjectArray arrayWithObjects:(id[]){ create_JavaLangAnnotationRetention(JreLoadEnum(JavaLangAnnotationRetentionPolicy, SOURCE)), create_JavaLangAnnotationTarget([IOSObjectArray arrayWithObjects:(id[]){ JreLoadEnum(JavaLangAnnotationElementType, TYPE) } count:1 type:JavaLangAnnotationElementType_class_()]) } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleAutoValueAutoValue_Builder)

@implementation ComGoogleAutoValueAutoValue_CopyAnnotations

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "[LIOSClass;", 0x401, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(exclude);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = { "LComGoogleAutoValueAutoValue;", (void *)&ComGoogleAutoValueAutoValue_CopyAnnotations__Annotations$0 };
  static const J2ObjcClassInfo _ComGoogleAutoValueAutoValue_CopyAnnotations = { "CopyAnnotations", "com.google.auto.value", ptrTable, methods, NULL, 7, 0x2609, 1, 0, 0, -1, -1, -1, 1 };
  return &_ComGoogleAutoValueAutoValue_CopyAnnotations;
}

@end

IOSObjectArray *ComGoogleAutoValueAutoValue_CopyAnnotations__Annotations$0() {
  return [IOSObjectArray arrayWithObjects:(id[]){ create_JavaLangAnnotationRetention(JreLoadEnum(JavaLangAnnotationRetentionPolicy, SOURCE)), create_JavaLangAnnotationTarget([IOSObjectArray arrayWithObjects:(id[]){ JreLoadEnum(JavaLangAnnotationElementType, TYPE), JreLoadEnum(JavaLangAnnotationElementType, METHOD) } count:2 type:JavaLangAnnotationElementType_class_()]) } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleAutoValueAutoValue_CopyAnnotations)
