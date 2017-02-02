#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class FIROptions;

NS_ASSUME_NONNULL_BEGIN

/** A block that takes a BOOL and has no return value. */
typedef void (^FIRAppVoidBoolCallback)(BOOL success);

/**
 * The entry point of Firebase SDKs.
 *
 * Initialize and configure FIRApp using +[FIRApp configure]
 * or other customized ways as shown below.
 *
 * The logging system has two modes: default mode and debug mode. In default mode, only logs with
 * log level Notice, Warning and Error will be sent to device. In debug mode, all logs will be sent
 * to device. The log levels that Firebase uses are consistent with the ASL log levels.
 *
 * Enable debug mode by passing the -FIRDebugEnabled argument to the application. You can add this
 * argument in the application's Xcode scheme. When debug mode is enabled via -FIRDebugEnabled,
 * further executions of the application will also be in debug mode. In order to return to default
 * mode, you must explicitly disable the debug mode with the application argument -FIRDebugDisabled.
 */
@interface FIRApp : NSObject

/**
 * Configures a default Firebase app. Raises an exception if any configuration step fails. The
 * default app is named "__FIRAPP_DEFAULT". This method should be called after the app is launched
 * and before using Firebase services. This method is thread safe.
 */
+ (void)configure;

/**
 * Configures the default Firebase app with the provided options. The default app is named
 * "__FIRAPP_DEFAULT". Raises an exception if any configuration step fails. This method is thread
 * safe.
 *
 * @param options The Firebase application options used to configure the service.
 */
+ (void)configureWithOptions:(FIROptions *)options;

/**
 * Configures a Firebase app with the given name and options. Raises an exception if any
 * configuration step fails. This method is thread safe.
 *
 * @param name The application's name given by the developer. The name should should only contain
               Letters, Numbers and Underscore.
 * @param options The Firebase application options used to configure the services.
 */
+ (void)configureWithName:(NSString *)name options:(FIROptions *)options;

/**
 * Returns the default app, or nil if the default app does not exist.
 */
+ (nullable FIRApp *)defaultApp NS_SWIFT_NAME(defaultApp());

/**
 * Returns a previously created FIRApp instance with the given name, or nil if no such app exists.
 * This method is thread safe.
 */
+ (nullable FIRApp *)appNamed:(NSString *)name;

/**
 * Returns the set of all extant FIRApp instances, or nil if there are no FIRApp instances. This
 * method is thread safe.
 */
+ (nullable NSDictionary *)allApps;

/**
 * Cleans up the current FIRApp, freeing associated data and returning its name to the pool for
 * future use. This method is thread safe.
 */
- (void)deleteApp:(FIRAppVoidBoolCallback)completion;

/**
 * FIRApp instances should not be initialized directly. Call +[FIRApp configure],
 * +[FIRApp configureWithOptions:], or +[FIRApp configureWithNames:options:] directly.
 */
- (instancetype)init NS_UNAVAILABLE;

/**
 * Gets the name of this app.
 */
@property(nonatomic, copy, readonly) NSString *name;

/**
 * Gets the options for this app.
 */
@property(nonatomic, readonly) FIROptions *options;

@end

NS_ASSUME_NONNULL_END
