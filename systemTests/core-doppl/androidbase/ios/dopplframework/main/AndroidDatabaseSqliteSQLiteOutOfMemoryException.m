//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "AndroidDatabaseSqliteSQLiteException.h"
#include "AndroidDatabaseSqliteSQLiteOutOfMemoryException.h"
#include "J2ObjC_source.h"

@implementation AndroidDatabaseSqliteSQLiteOutOfMemoryException

J2OBJC_IGNORE_DESIGNATED_BEGIN
- (instancetype)init {
  AndroidDatabaseSqliteSQLiteOutOfMemoryException_init(self);
  return self;
}
J2OBJC_IGNORE_DESIGNATED_END

- (instancetype)initWithNSString:(NSString *)error {
  AndroidDatabaseSqliteSQLiteOutOfMemoryException_initWithNSString_(self, error);
  return self;
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(initWithNSString:);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = { "LNSString;" };
  static const J2ObjcClassInfo _AndroidDatabaseSqliteSQLiteOutOfMemoryException = { "SQLiteOutOfMemoryException", "android.database.sqlite", ptrTable, methods, NULL, 7, 0x1, 2, 0, -1, -1, -1, -1, -1 };
  return &_AndroidDatabaseSqliteSQLiteOutOfMemoryException;
}

@end

void AndroidDatabaseSqliteSQLiteOutOfMemoryException_init(AndroidDatabaseSqliteSQLiteOutOfMemoryException *self) {
  AndroidDatabaseSqliteSQLiteException_init(self);
}

AndroidDatabaseSqliteSQLiteOutOfMemoryException *new_AndroidDatabaseSqliteSQLiteOutOfMemoryException_init() {
  J2OBJC_NEW_IMPL(AndroidDatabaseSqliteSQLiteOutOfMemoryException, init)
}

AndroidDatabaseSqliteSQLiteOutOfMemoryException *create_AndroidDatabaseSqliteSQLiteOutOfMemoryException_init() {
  J2OBJC_CREATE_IMPL(AndroidDatabaseSqliteSQLiteOutOfMemoryException, init)
}

void AndroidDatabaseSqliteSQLiteOutOfMemoryException_initWithNSString_(AndroidDatabaseSqliteSQLiteOutOfMemoryException *self, NSString *error) {
  AndroidDatabaseSqliteSQLiteException_initWithNSString_(self, error);
}

AndroidDatabaseSqliteSQLiteOutOfMemoryException *new_AndroidDatabaseSqliteSQLiteOutOfMemoryException_initWithNSString_(NSString *error) {
  J2OBJC_NEW_IMPL(AndroidDatabaseSqliteSQLiteOutOfMemoryException, initWithNSString_, error)
}

AndroidDatabaseSqliteSQLiteOutOfMemoryException *create_AndroidDatabaseSqliteSQLiteOutOfMemoryException_initWithNSString_(NSString *error) {
  J2OBJC_CREATE_IMPL(AndroidDatabaseSqliteSQLiteOutOfMemoryException, initWithNSString_, error)
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AndroidDatabaseSqliteSQLiteOutOfMemoryException)
