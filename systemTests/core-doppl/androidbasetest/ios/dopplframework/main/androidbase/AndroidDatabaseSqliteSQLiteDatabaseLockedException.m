//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "AndroidDatabaseSqliteSQLiteDatabaseLockedException.h"
#include "AndroidDatabaseSqliteSQLiteException.h"
#include "J2ObjC_source.h"

@implementation AndroidDatabaseSqliteSQLiteDatabaseLockedException

J2OBJC_IGNORE_DESIGNATED_BEGIN
- (instancetype)init {
  AndroidDatabaseSqliteSQLiteDatabaseLockedException_init(self);
  return self;
}
J2OBJC_IGNORE_DESIGNATED_END

- (instancetype)initWithNSString:(NSString *)error {
  AndroidDatabaseSqliteSQLiteDatabaseLockedException_initWithNSString_(self, error);
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
  static const J2ObjcClassInfo _AndroidDatabaseSqliteSQLiteDatabaseLockedException = { "SQLiteDatabaseLockedException", "android.database.sqlite", ptrTable, methods, NULL, 7, 0x1, 2, 0, -1, -1, -1, -1, -1 };
  return &_AndroidDatabaseSqliteSQLiteDatabaseLockedException;
}

@end

void AndroidDatabaseSqliteSQLiteDatabaseLockedException_init(AndroidDatabaseSqliteSQLiteDatabaseLockedException *self) {
  AndroidDatabaseSqliteSQLiteException_init(self);
}

AndroidDatabaseSqliteSQLiteDatabaseLockedException *new_AndroidDatabaseSqliteSQLiteDatabaseLockedException_init() {
  J2OBJC_NEW_IMPL(AndroidDatabaseSqliteSQLiteDatabaseLockedException, init)
}

AndroidDatabaseSqliteSQLiteDatabaseLockedException *create_AndroidDatabaseSqliteSQLiteDatabaseLockedException_init() {
  J2OBJC_CREATE_IMPL(AndroidDatabaseSqliteSQLiteDatabaseLockedException, init)
}

void AndroidDatabaseSqliteSQLiteDatabaseLockedException_initWithNSString_(AndroidDatabaseSqliteSQLiteDatabaseLockedException *self, NSString *error) {
  AndroidDatabaseSqliteSQLiteException_initWithNSString_(self, error);
}

AndroidDatabaseSqliteSQLiteDatabaseLockedException *new_AndroidDatabaseSqliteSQLiteDatabaseLockedException_initWithNSString_(NSString *error) {
  J2OBJC_NEW_IMPL(AndroidDatabaseSqliteSQLiteDatabaseLockedException, initWithNSString_, error)
}

AndroidDatabaseSqliteSQLiteDatabaseLockedException *create_AndroidDatabaseSqliteSQLiteDatabaseLockedException_initWithNSString_(NSString *error) {
  J2OBJC_CREATE_IMPL(AndroidDatabaseSqliteSQLiteDatabaseLockedException, initWithNSString_, error)
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AndroidDatabaseSqliteSQLiteDatabaseLockedException)
