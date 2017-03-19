//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "AndroidOsTrace.h"
#include "J2ObjC_source.h"
#include "java/lang/IllegalArgumentException.h"

@interface AndroidOsTrace ()

+ (jlong)nativeGetEnabledTags;

+ (void)nativeTraceCounterWithLong:(jlong)tag
                      withNSString:(NSString *)name
                           withInt:(jint)value;

+ (void)nativeTraceBeginWithLong:(jlong)tag
                    withNSString:(NSString *)name;

+ (void)nativeTraceEndWithLong:(jlong)tag;

+ (void)nativeAsyncTraceBeginWithLong:(jlong)tag
                         withNSString:(NSString *)name
                              withInt:(jint)cookie;

+ (void)nativeAsyncTraceEndWithLong:(jlong)tag
                       withNSString:(NSString *)name
                            withInt:(jint)cookie;

+ (void)nativeSetAppTracingAllowedWithBoolean:(jboolean)allowed;

+ (void)nativeSetTracingEnabledWithBoolean:(jboolean)allowed;

- (instancetype)init;

+ (jlong)cacheEnabledTags;

@end

inline NSString *AndroidOsTrace_get_TAG();
static NSString *AndroidOsTrace_TAG = @"Trace";
J2OBJC_STATIC_FIELD_OBJ_FINAL(AndroidOsTrace, TAG, NSString *)

inline jlong AndroidOsTrace_get_TRACE_TAG_NOT_READY();
#define AndroidOsTrace_TRACE_TAG_NOT_READY ((jlong) 0x8000000000000000LL)
J2OBJC_STATIC_FIELD_CONSTANT(AndroidOsTrace, TRACE_TAG_NOT_READY, jlong)

inline jint AndroidOsTrace_get_MAX_SECTION_NAME_LEN();
#define AndroidOsTrace_MAX_SECTION_NAME_LEN 127
J2OBJC_STATIC_FIELD_CONSTANT(AndroidOsTrace, MAX_SECTION_NAME_LEN, jint)

inline jlong AndroidOsTrace_get_sEnabledTags();
inline jlong AndroidOsTrace_set_sEnabledTags(jlong value);
static volatile_jlong AndroidOsTrace_sEnabledTags = -9223372036854775808;
J2OBJC_STATIC_FIELD_PRIMITIVE_VOLATILE(AndroidOsTrace, sEnabledTags, jlong)

__attribute__((unused)) static jlong AndroidOsTrace_nativeGetEnabledTags();

__attribute__((unused)) static void AndroidOsTrace_nativeTraceCounterWithLong_withNSString_withInt_(jlong tag, NSString *name, jint value);

__attribute__((unused)) static void AndroidOsTrace_nativeTraceBeginWithLong_withNSString_(jlong tag, NSString *name);

__attribute__((unused)) static void AndroidOsTrace_nativeTraceEndWithLong_(jlong tag);

__attribute__((unused)) static void AndroidOsTrace_nativeAsyncTraceBeginWithLong_withNSString_withInt_(jlong tag, NSString *name, jint cookie);

__attribute__((unused)) static void AndroidOsTrace_nativeAsyncTraceEndWithLong_withNSString_withInt_(jlong tag, NSString *name, jint cookie);

__attribute__((unused)) static void AndroidOsTrace_nativeSetAppTracingAllowedWithBoolean_(jboolean allowed);

__attribute__((unused)) static void AndroidOsTrace_nativeSetTracingEnabledWithBoolean_(jboolean allowed);

__attribute__((unused)) static void AndroidOsTrace_init(AndroidOsTrace *self);

__attribute__((unused)) static AndroidOsTrace *new_AndroidOsTrace_init() NS_RETURNS_RETAINED;

__attribute__((unused)) static AndroidOsTrace *create_AndroidOsTrace_init();

__attribute__((unused)) static jlong AndroidOsTrace_cacheEnabledTags();

J2OBJC_INITIALIZED_DEFN(AndroidOsTrace)

@implementation AndroidOsTrace

+ (jlong)nativeGetEnabledTags {
  return AndroidOsTrace_nativeGetEnabledTags();
}

+ (void)nativeTraceCounterWithLong:(jlong)tag
                      withNSString:(NSString *)name
                           withInt:(jint)value {
  AndroidOsTrace_nativeTraceCounterWithLong_withNSString_withInt_(tag, name, value);
}

+ (void)nativeTraceBeginWithLong:(jlong)tag
                    withNSString:(NSString *)name {
  AndroidOsTrace_nativeTraceBeginWithLong_withNSString_(tag, name);
}

+ (void)nativeTraceEndWithLong:(jlong)tag {
  AndroidOsTrace_nativeTraceEndWithLong_(tag);
}

+ (void)nativeAsyncTraceBeginWithLong:(jlong)tag
                         withNSString:(NSString *)name
                              withInt:(jint)cookie {
  AndroidOsTrace_nativeAsyncTraceBeginWithLong_withNSString_withInt_(tag, name, cookie);
}

+ (void)nativeAsyncTraceEndWithLong:(jlong)tag
                       withNSString:(NSString *)name
                            withInt:(jint)cookie {
  AndroidOsTrace_nativeAsyncTraceEndWithLong_withNSString_withInt_(tag, name, cookie);
}

+ (void)nativeSetAppTracingAllowedWithBoolean:(jboolean)allowed {
  AndroidOsTrace_nativeSetAppTracingAllowedWithBoolean_(allowed);
}

+ (void)nativeSetTracingEnabledWithBoolean:(jboolean)allowed {
  AndroidOsTrace_nativeSetTracingEnabledWithBoolean_(allowed);
}

J2OBJC_IGNORE_DESIGNATED_BEGIN
- (instancetype)init {
  AndroidOsTrace_init(self);
  return self;
}
J2OBJC_IGNORE_DESIGNATED_END

+ (jlong)cacheEnabledTags {
  return AndroidOsTrace_cacheEnabledTags();
}

+ (jboolean)isTagEnabledWithLong:(jlong)traceTag {
  return AndroidOsTrace_isTagEnabledWithLong_(traceTag);
}

+ (void)traceCounterWithLong:(jlong)traceTag
                withNSString:(NSString *)counterName
                     withInt:(jint)counterValue {
  AndroidOsTrace_traceCounterWithLong_withNSString_withInt_(traceTag, counterName, counterValue);
}

+ (void)setAppTracingAllowedWithBoolean:(jboolean)allowed {
  AndroidOsTrace_setAppTracingAllowedWithBoolean_(allowed);
}

+ (void)setTracingEnabledWithBoolean:(jboolean)enabled {
  AndroidOsTrace_setTracingEnabledWithBoolean_(enabled);
}

+ (void)traceBeginWithLong:(jlong)traceTag
              withNSString:(NSString *)methodName {
  AndroidOsTrace_traceBeginWithLong_withNSString_(traceTag, methodName);
}

+ (void)traceEndWithLong:(jlong)traceTag {
  AndroidOsTrace_traceEndWithLong_(traceTag);
}

+ (void)asyncTraceBeginWithLong:(jlong)traceTag
                   withNSString:(NSString *)methodName
                        withInt:(jint)cookie {
  AndroidOsTrace_asyncTraceBeginWithLong_withNSString_withInt_(traceTag, methodName, cookie);
}

+ (void)asyncTraceEndWithLong:(jlong)traceTag
                 withNSString:(NSString *)methodName
                      withInt:(jint)cookie {
  AndroidOsTrace_asyncTraceEndWithLong_withNSString_withInt_(traceTag, methodName, cookie);
}

+ (void)beginSectionWithNSString:(NSString *)sectionName {
  AndroidOsTrace_beginSectionWithNSString_(sectionName);
}

+ (void)endSection {
  AndroidOsTrace_endSection();
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "J", 0x10a, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 0, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 2, 3, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 4, 5, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 6, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 7, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 8, 9, -1, -1, -1, -1 },
    { NULL, "V", 0x10a, 10, 9, -1, -1, -1, -1 },
    { NULL, NULL, 0x2, -1, -1, -1, -1, -1, -1 },
    { NULL, "J", 0xa, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x9, 11, 5, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 12, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 13, 9, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 14, 9, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 15, 3, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 16, 5, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 17, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 18, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 19, 20, -1, -1, -1, -1 },
    { NULL, "V", 0x9, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(nativeGetEnabledTags);
  methods[1].selector = @selector(nativeTraceCounterWithLong:withNSString:withInt:);
  methods[2].selector = @selector(nativeTraceBeginWithLong:withNSString:);
  methods[3].selector = @selector(nativeTraceEndWithLong:);
  methods[4].selector = @selector(nativeAsyncTraceBeginWithLong:withNSString:withInt:);
  methods[5].selector = @selector(nativeAsyncTraceEndWithLong:withNSString:withInt:);
  methods[6].selector = @selector(nativeSetAppTracingAllowedWithBoolean:);
  methods[7].selector = @selector(nativeSetTracingEnabledWithBoolean:);
  methods[8].selector = @selector(init);
  methods[9].selector = @selector(cacheEnabledTags);
  methods[10].selector = @selector(isTagEnabledWithLong:);
  methods[11].selector = @selector(traceCounterWithLong:withNSString:withInt:);
  methods[12].selector = @selector(setAppTracingAllowedWithBoolean:);
  methods[13].selector = @selector(setTracingEnabledWithBoolean:);
  methods[14].selector = @selector(traceBeginWithLong:withNSString:);
  methods[15].selector = @selector(traceEndWithLong:);
  methods[16].selector = @selector(asyncTraceBeginWithLong:withNSString:withInt:);
  methods[17].selector = @selector(asyncTraceEndWithLong:withNSString:withInt:);
  methods[18].selector = @selector(beginSectionWithNSString:);
  methods[19].selector = @selector(endSection);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "TAG", "LNSString;", .constantValue.asLong = 0, 0x1a, -1, 21, -1, -1 },
    { "TRACE_TAG_NEVER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_NEVER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_ALWAYS", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_ALWAYS, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_GRAPHICS", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_GRAPHICS, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_INPUT", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_INPUT, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_VIEW", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_VIEW, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_WEBVIEW", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_WEBVIEW, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_WINDOW_MANAGER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_WINDOW_MANAGER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_ACTIVITY_MANAGER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_ACTIVITY_MANAGER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_SYNC_MANAGER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_SYNC_MANAGER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_AUDIO", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_AUDIO, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_VIDEO", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_VIDEO, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_CAMERA", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_CAMERA, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_HAL", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_HAL, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_APP", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_APP, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_RESOURCES", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_RESOURCES, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_DALVIK", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_DALVIK, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_RS", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_RS, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_BIONIC", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_BIONIC, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_POWER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_POWER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_PACKAGE_MANAGER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_PACKAGE_MANAGER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_SYSTEM_SERVER", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_SYSTEM_SERVER, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_DATABASE", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_DATABASE, 0x19, -1, -1, -1, -1 },
    { "TRACE_TAG_NOT_READY", "J", .constantValue.asLong = AndroidOsTrace_TRACE_TAG_NOT_READY, 0x1a, -1, -1, -1, -1 },
    { "MAX_SECTION_NAME_LEN", "I", .constantValue.asInt = AndroidOsTrace_MAX_SECTION_NAME_LEN, 0x1a, -1, -1, -1, -1 },
    { "sEnabledTags", "J", .constantValue.asLong = 0, 0x4a, -1, 22, -1, -1 },
  };
  static const void *ptrTable[] = { "nativeTraceCounter", "JLNSString;I", "nativeTraceBegin", "JLNSString;", "nativeTraceEnd", "J", "nativeAsyncTraceBegin", "nativeAsyncTraceEnd", "nativeSetAppTracingAllowed", "Z", "nativeSetTracingEnabled", "isTagEnabled", "traceCounter", "setAppTracingAllowed", "setTracingEnabled", "traceBegin", "traceEnd", "asyncTraceBegin", "asyncTraceEnd", "beginSection", "LNSString;", &AndroidOsTrace_TAG, &AndroidOsTrace_sEnabledTags };
  static const J2ObjcClassInfo _AndroidOsTrace = { "Trace", "android.os", ptrTable, methods, fields, 7, 0x11, 20, 26, -1, -1, -1, -1, -1 };
  return &_AndroidOsTrace;
}

+ (void)initialize {
  if (self == [AndroidOsTrace class]) {
    {
    }
    J2OBJC_SET_INITIALIZED(AndroidOsTrace)
  }
}

@end

jlong AndroidOsTrace_nativeGetEnabledTags() {
  AndroidOsTrace_initialize();
  return -1;
}

void AndroidOsTrace_nativeTraceCounterWithLong_withNSString_withInt_(jlong tag, NSString *name, jint value) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeTraceBeginWithLong_withNSString_(jlong tag, NSString *name) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeTraceEndWithLong_(jlong tag) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeAsyncTraceBeginWithLong_withNSString_withInt_(jlong tag, NSString *name, jint cookie) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeAsyncTraceEndWithLong_withNSString_withInt_(jlong tag, NSString *name, jint cookie) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeSetAppTracingAllowedWithBoolean_(jboolean allowed) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_nativeSetTracingEnabledWithBoolean_(jboolean allowed) {
  AndroidOsTrace_initialize();
  
}

void AndroidOsTrace_init(AndroidOsTrace *self) {
  NSObject_init(self);
}

AndroidOsTrace *new_AndroidOsTrace_init() {
  J2OBJC_NEW_IMPL(AndroidOsTrace, init)
}

AndroidOsTrace *create_AndroidOsTrace_init() {
  J2OBJC_CREATE_IMPL(AndroidOsTrace, init)
}

jlong AndroidOsTrace_cacheEnabledTags() {
  AndroidOsTrace_initialize();
  jlong tags = AndroidOsTrace_nativeGetEnabledTags();
  JreAssignVolatileLong(&AndroidOsTrace_sEnabledTags, tags);
  return tags;
}

jboolean AndroidOsTrace_isTagEnabledWithLong_(jlong traceTag) {
  AndroidOsTrace_initialize();
  jlong tags = JreLoadVolatileLong(&AndroidOsTrace_sEnabledTags);
  if (tags == AndroidOsTrace_TRACE_TAG_NOT_READY) {
    tags = AndroidOsTrace_cacheEnabledTags();
  }
  return (tags & traceTag) != 0;
}

void AndroidOsTrace_traceCounterWithLong_withNSString_withInt_(jlong traceTag, NSString *counterName, jint counterValue) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(traceTag)) {
    AndroidOsTrace_nativeTraceCounterWithLong_withNSString_withInt_(traceTag, counterName, counterValue);
  }
}

void AndroidOsTrace_setAppTracingAllowedWithBoolean_(jboolean allowed) {
  AndroidOsTrace_initialize();
  AndroidOsTrace_nativeSetAppTracingAllowedWithBoolean_(allowed);
  AndroidOsTrace_cacheEnabledTags();
}

void AndroidOsTrace_setTracingEnabledWithBoolean_(jboolean enabled) {
  AndroidOsTrace_initialize();
  AndroidOsTrace_nativeSetTracingEnabledWithBoolean_(enabled);
  AndroidOsTrace_cacheEnabledTags();
}

void AndroidOsTrace_traceBeginWithLong_withNSString_(jlong traceTag, NSString *methodName) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(traceTag)) {
    AndroidOsTrace_nativeTraceBeginWithLong_withNSString_(traceTag, methodName);
  }
}

void AndroidOsTrace_traceEndWithLong_(jlong traceTag) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(traceTag)) {
    AndroidOsTrace_nativeTraceEndWithLong_(traceTag);
  }
}

void AndroidOsTrace_asyncTraceBeginWithLong_withNSString_withInt_(jlong traceTag, NSString *methodName, jint cookie) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(traceTag)) {
    AndroidOsTrace_nativeAsyncTraceBeginWithLong_withNSString_withInt_(traceTag, methodName, cookie);
  }
}

void AndroidOsTrace_asyncTraceEndWithLong_withNSString_withInt_(jlong traceTag, NSString *methodName, jint cookie) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(traceTag)) {
    AndroidOsTrace_nativeAsyncTraceEndWithLong_withNSString_withInt_(traceTag, methodName, cookie);
  }
}

void AndroidOsTrace_beginSectionWithNSString_(NSString *sectionName) {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(AndroidOsTrace_TRACE_TAG_APP)) {
    if (((jint) [((NSString *) nil_chk(sectionName)) length]) > AndroidOsTrace_MAX_SECTION_NAME_LEN) {
      @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"sectionName is too long");
    }
    AndroidOsTrace_nativeTraceBeginWithLong_withNSString_(AndroidOsTrace_TRACE_TAG_APP, sectionName);
  }
}

void AndroidOsTrace_endSection() {
  AndroidOsTrace_initialize();
  if (AndroidOsTrace_isTagEnabledWithLong_(AndroidOsTrace_TRACE_TAG_APP)) {
    AndroidOsTrace_nativeTraceEndWithLong_(AndroidOsTrace_TRACE_TAG_APP);
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AndroidOsTrace)
