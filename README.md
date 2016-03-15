[ ![Download](https://api.bintray.com/packages/chriscm2006/maven/chris-logger/images/download.svg) ](https://bintray.com/chriscm2006/maven/chris-logger/_latestVersion)
# ChrisLogger
A simple logging extension to the Android logcat API.  Rather than specifying a log tag for each message tags are calculated by their class, function call, and line numbers.  For release a configurable default tag is added to messages and log levels less than warning (configurable) are ignored completely.

## Installation
For now this project is only available for use as a submodule.  Add the submodule to your project under the root directory.  Assuming you use the path "ChrisLogger" you would then do the following:

In your settings.gradle file

    include ':ChrisLogger:chrisloggerlibrary', ':app'

In your apps build.gradle file:
    
    ...
    dependencies {
      ...
      compile project(':ChrisLogger:chrisloggerlibrary')
      ...
    }
    ...

Finally, this libary is heavily dependant on whether you are in debug or release mode.  My method of using function calls and class names as log tags is something you obviously would prefer leaving out of released code.  The Android Gradle system for allowing separate config/debug library builds is currently very clumsy.  As such, you must initialize the logger with two critical pieces of information, prior to any logging call.  This is best done in a static initializer block of your code entry class.

    class MyActivity extends Activity {
    
      static {
        CLog.initialize("ReleaseLogTag", BuildConfig.DEBUG);
      }
    }
This will cause all messages in release mode to have a tag of "ReleaseLogTag" and the debug flag should be based off of your projects build configuration.

## Basic Usage
There are two public classes available to create log messages.  You can either use the Log class, which is a class full of static methods.  Or you can create your own logger objects, and use them directly.

    class MyClass {
      public myFunction() {
        Log.w("A message"); //The same as Log.w("MyClass", "A message");
      }
    }
Or by creating your own logger object.

    class MyClass {
      Logger logger = new Logger();
      
      public myFunction() {
        logger.w("A message"); //The same as Log.w("MyClass", "A message");
      }
    }
The two examples will create a logCat message that looks like this:
    W/MyClass.myFunction/###: A Message
Where ### is the line number of the log message.
## Disable Logging for a Class
Sometimes it is useful to ignore logging from an overly verbose class.  This is very simple.

    CLog.getLogger(MyActivity.class).setLogLevel(LogLevel.ASSERT);

Now logs associated with the "MyActivity" class will only log if their of the highest priority.  Juse use:

    CLog.getLogger(MyActivity.class).setLogLevel(LogLevel.VERBOSE);
    
to re-enable all logs.

## Elevate Logging for a Class
Sometimes you only care about logging for a given class.  Some may note that I have not included convenience methods for the "info" level associated with LogCat.  If we do the following:

    CLog.getLogger(MyActivity.class).setIsImportant(true);
    
any VERBOSE or DEBUG logs coming from MyActivity will log on the info channel.  This allows you to ignore any VERBOSE or DEBUG logs from anywhere else!
