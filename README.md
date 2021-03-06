# ChrisLogger
A simple logging extension to the Android logcat API.  Rather than specifying a log tag for each message tags are calculated by their class, function call, and line numbers.  This allows you to click on the file and go directly to the line that created the log message in Android Studio.  This does slow things down a bit, so if you need to test performance, ensure you set the DEBUG flag to false upon initialization!

## Installation
In your build.gradle file

    repositories {
        maven {url "http://jitpack.io"}
    }
    dependencies {
      compile 'com.github.chriscm2006:ChrisLogger:0.1.1'
    }

Finally, this library is heavily dependant on whether you are in debug or release mode.  My method of using function calls and class names as log tags is something you obviously would prefer leaving out of released code.  The Android Gradle system for allowing separate config/debug library builds is currently very clumsy.  As such, you must initialize the logger with two critical pieces of information, prior to any logging call.  This is best done in a static initializer block of your code entry class.

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

Now logs associated with the "MyActivity" class will only log if their of the highest priority.  Just use:

    CLog.getLogger(MyActivity.class).setLogLevel(LogLevel.VERBOSE);

to re-enable all logs.

## Elevate Logging for a Class
Sometimes you only care about logging for a given class.  Some may note that I have not included convenience methods for the "info" level associated with LogCat.  If we do the following:

    CLog.getLogger(MyActivity.class).setIsImportant(true);

any VERBOSE or DEBUG logs coming from MyActivity will log on the info channel.  This allows you to ignore any VERBOSE or DEBUG logs from anywhere else!
