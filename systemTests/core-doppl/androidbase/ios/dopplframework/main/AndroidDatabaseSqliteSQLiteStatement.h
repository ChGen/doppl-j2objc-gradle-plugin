//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//

#include "J2ObjC_header.h"

#pragma push_macro("INCLUDE_ALL_AndroidDatabaseSqliteSQLiteStatement")
#ifdef RESTRICT_AndroidDatabaseSqliteSQLiteStatement
#define INCLUDE_ALL_AndroidDatabaseSqliteSQLiteStatement 0
#else
#define INCLUDE_ALL_AndroidDatabaseSqliteSQLiteStatement 1
#endif
#undef RESTRICT_AndroidDatabaseSqliteSQLiteStatement

#if !defined (AndroidDatabaseSqliteSQLiteStatement_) && (INCLUDE_ALL_AndroidDatabaseSqliteSQLiteStatement || defined(INCLUDE_AndroidDatabaseSqliteSQLiteStatement))
#define AndroidDatabaseSqliteSQLiteStatement_

#define RESTRICT_AndroidDatabaseSqliteSQLiteProgram 1
#define INCLUDE_AndroidDatabaseSqliteSQLiteProgram 1
#include "AndroidDatabaseSqliteSQLiteProgram.h"

@class AndroidDatabaseSqliteSQLiteDatabase;
@class IOSObjectArray;

@interface AndroidDatabaseSqliteSQLiteStatement : AndroidDatabaseSqliteSQLiteProgram

#pragma mark Public

- (void)execute;

- (jlong)executeInsert;

- (jint)executeUpdateDelete;

- (jlong)simpleQueryForLong;

- (NSString *)simpleQueryForString;

- (NSString *)description;

#pragma mark Package-Private

- (instancetype)initWithAndroidDatabaseSqliteSQLiteDatabase:(AndroidDatabaseSqliteSQLiteDatabase *)db
                                               withNSString:(NSString *)sql
                                          withNSObjectArray:(IOSObjectArray *)bindArgs;

@end

J2OBJC_EMPTY_STATIC_INIT(AndroidDatabaseSqliteSQLiteStatement)

FOUNDATION_EXPORT void AndroidDatabaseSqliteSQLiteStatement_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSObjectArray_(AndroidDatabaseSqliteSQLiteStatement *self, AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, IOSObjectArray *bindArgs);

FOUNDATION_EXPORT AndroidDatabaseSqliteSQLiteStatement *new_AndroidDatabaseSqliteSQLiteStatement_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSObjectArray_(AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, IOSObjectArray *bindArgs) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT AndroidDatabaseSqliteSQLiteStatement *create_AndroidDatabaseSqliteSQLiteStatement_initWithAndroidDatabaseSqliteSQLiteDatabase_withNSString_withNSObjectArray_(AndroidDatabaseSqliteSQLiteDatabase *db, NSString *sql, IOSObjectArray *bindArgs);

J2OBJC_TYPE_LITERAL_HEADER(AndroidDatabaseSqliteSQLiteStatement)

#endif

#pragma pop_macro("INCLUDE_ALL_AndroidDatabaseSqliteSQLiteStatement")
