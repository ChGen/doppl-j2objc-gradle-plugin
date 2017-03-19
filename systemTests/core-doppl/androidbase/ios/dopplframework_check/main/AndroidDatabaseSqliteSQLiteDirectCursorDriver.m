//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "AndroidDatabaseCursor.h"
#include "AndroidDatabaseSqliteSQLiteCursor.h"
#include "AndroidDatabaseSqliteSQLiteDatabase.h"
#include "AndroidDatabaseSqliteSQLiteDirectCursorDriver.h"
#include "AndroidDatabaseSqliteSQLiteQuery.h"
#include "IOSObjectArray.h"
#include "J2ObjC_source.h"
#include "java/lang/RuntimeException.h"

@interface AndroidDatabaseSqliteSQLiteDirectCursorDriver () {
 @public
  AndroidDatabaseSqliteSQLiteDatabase *mDatabase_;
  NSString *mEditTable_;
  NSString *mSql_;
  AndroidDatabaseSqliteSQLiteQuery *mQuery_;
}

@end

J2OBJC_FIELD_SETTER(AndroidDatabaseSqliteSQLiteDirectCursorDriver, mDatabase_, AndroidDatabaseSqliteSQLiteDatabase *)
J2OBJC_FIELD_SETTER(AndroidDatabaseSqliteSQLiteDirectCursorDriver, mEditTable_, NSString *)
J2OBJC_FIELD_SETTER(AndroidDatabaseSqliteSQLiteDirectCursorDriver, mSql_, NSString *)
J2OBJC_FIELD_SETTER(AndroidDatabaseSqliteSQLiteDirectCursorDriver, mQuery_, AndroidDatabaseSqliteSQLiteQuery *)

@implementation AndroidDatabaseSqliteSQLiteDirectCursorDriver

- (instancetype)initWithAndroidDatabaseSqliteSQLiteDatabase:(AndroidDatabaseSqliteSQLiteDatabase *)db
                                               withNSString:(NSString *)sql
                                               withNSString:(NSString *)editTable {
  AndroidDatabaseSqliteSQLiteDirectCursorDriver_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_(self, db, sql, editTable);
  return self;
}

- (id<AndroidDatabaseCursor>)queryWithAndroidDatabaseSqliteSQLiteDatabase_CursorFactory:(id<AndroidDatabaseSqliteSQLiteDatabase_CursorFactory>)factory
                                                                      withNSStringArray:(IOSObjectArray *)selectionArgs {
  AndroidDatabaseSqliteSQLiteQuery *query = create_AndroidDatabaseSqliteSQLiteQuery_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_(mDatabase_, mSql_);
  id<AndroidDatabaseCursor> cursor;
  @try {
    [query bindAllArgsAsStringsWithNSStringArray:selectionArgs];
    if (factory == nil) {
      cursor = create_AndroidDatabaseSqliteSQLiteCursor_initWithAndroidDatabaseSqliteSQLiteCursorDriver_withNSString_withAndroidDatabaseSqliteSQLiteQuery_(self, mEditTable_, query);
    }
    else {
      cursor = [factory newCursorWithAndroidDatabaseSqliteSQLiteDatabase:mDatabase_ withAndroidDatabaseSqliteSQLiteCursorDriver:self withNSString:mEditTable_ withAndroidDatabaseSqliteSQLiteQuery:query];
    }
  }
  @catch (JavaLangRuntimeException *ex) {
    [query close];
    @throw ex;
  }
  JreStrongAssign(&mQuery_, query);
  return cursor;
}

- (void)cursorClosed {
}

- (void)setBindArgumentsWithNSStringArray:(IOSObjectArray *)bindArgs {
  [((AndroidDatabaseSqliteSQLiteQuery *) nil_chk(mQuery_)) bindAllArgsAsStringsWithNSStringArray:bindArgs];
}

- (void)cursorDeactivated {
}

- (void)cursorRequeriedWithAndroidDatabaseCursor:(id<AndroidDatabaseCursor>)cursor {
}

- (NSString *)description {
  return JreStrcat("$$", @"SQLiteDirectCursorDriver: ", mSql_);
}

- (void)dealloc {
  RELEASE_(mDatabase_);
  RELEASE_(mEditTable_);
  RELEASE_(mSql_);
  RELEASE_(mQuery_);
  [super dealloc];
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1 },
    { NULL, "LAndroidDatabaseCursor;", 0x1, 1, 2, -1, -1, -1, -1 },
    { NULL, "V", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 3, 4, -1, -1, -1, -1 },
    { NULL, "V", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 5, 6, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 7, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(initWithAndroidDatabaseSqliteSQLiteDatabase:withNSString:withNSString:);
  methods[1].selector = @selector(queryWithAndroidDatabaseSqliteSQLiteDatabase_CursorFactory:withNSStringArray:);
  methods[2].selector = @selector(cursorClosed);
  methods[3].selector = @selector(setBindArgumentsWithNSStringArray:);
  methods[4].selector = @selector(cursorDeactivated);
  methods[5].selector = @selector(cursorRequeriedWithAndroidDatabaseCursor:);
  methods[6].selector = @selector(description);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "mDatabase_", "LAndroidDatabaseSqliteSQLiteDatabase;", .constantValue.asLong = 0, 0x12, -1, -1, -1, -1 },
    { "mEditTable_", "LNSString;", .constantValue.asLong = 0, 0x12, -1, -1, -1, -1 },
    { "mSql_", "LNSString;", .constantValue.asLong = 0, 0x12, -1, -1, -1, -1 },
    { "mQuery_", "LAndroidDatabaseSqliteSQLiteQuery;", .constantValue.asLong = 0, 0x2, -1, -1, -1, -1 },
  };
  static const void *ptrTable[] = { "LAndroidDatabaseSqliteSQLiteDatabase;LNSString;LNSString;", "query", "LAndroidDatabaseSqliteSQLiteDatabase_CursorFactory;[LNSString;", "setBindArguments", "[LNSString;", "cursorRequeried", "LAndroidDatabaseCursor;", "toString" };
  static const J2ObjcClassInfo _AndroidDatabaseSqliteSQLiteDirectCursorDriver = { "SQLiteDirectCursorDriver", "android.database.sqlite", ptrTable, methods, fields, 7, 0x11, 7, 4, -1, -1, -1, -1, -1 };
  return &_AndroidDatabaseSqliteSQLiteDirectCursorDriver;
}

@end

void AndroidDatabaseSqliteSQLiteDirectCursorDriver_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_(AndroidDatabaseSqliteSQLiteDirectCursorDriver *self, AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, NSString *editTable) {
  NSObject_init(self);
  JreStrongAssign(&self->mDatabase_, db);
  JreStrongAssign(&self->mEditTable_, editTable);
  JreStrongAssign(&self->mSql_, sql);
}

AndroidDatabaseSqliteSQLiteDirectCursorDriver *new_AndroidDatabaseSqliteSQLiteDirectCursorDriver_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_(AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, NSString *editTable) {
  J2OBJC_NEW_IMPL(AndroidDatabaseSqliteSQLiteDirectCursorDriver, initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_, db, sql, editTable)
}

AndroidDatabaseSqliteSQLiteDirectCursorDriver *create_AndroidDatabaseSqliteSQLiteDirectCursorDriver_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_(AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, NSString *editTable) {
  J2OBJC_CREATE_IMPL(AndroidDatabaseSqliteSQLiteDirectCursorDriver, initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSString_, db, sql, editTable)
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AndroidDatabaseSqliteSQLiteDirectCursorDriver)
