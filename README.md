# LED Cube Manager
You need ffmpeg to play audio. Get it at http://www.ffmpeg.org

Builds can be found at http://ci.techjargaming.com/job/LED%20Cube%20Manager/

## Configuration
Many settings can be changed in the software. There a few hidden settings, the config will be located in a directory based on your OS.

Windows: `%APPDATA%\.ledcubemanager`
Mac OS X: `~/Library/Application Support/ledcubemanager`
Linux: `~/.ledcubemanager`

## Building
You need to add lombok.jar (in lib directory) to your IDE's annotation processors.

## Modification
If you want to modify the LED Cube Manager for your own purposes, the first thing you need to do is implement `LEDManager`, located in
package `com.techjar.ledcm.hardware.manager`. See the existing classes which implement `LEDManager` for examples. The main thing to
change is `getCommData()`, as this is the method which constructs the byte array which will be sent over the serial port to your hardware.
You'll also need to appropriately size the arrays based on the cube dimensions, and modify the vector-encoding/decoding and LED-setting
methods to produce the correct indices from coordinates and vice-versa. You may also need to change some existing animations, as some of
them are expecting an 8x8x8 cube, and will not work correctly otherwise.

As for adding new animations, it is a fairly trivial matter. Simply extend `Animation`, located in `com.techjar.ledcm.hardware.animation`,
and change the methods appropriately. Once again, see existing classes for examples. You'll need to implement `getName()` which is the
name of the animation which will show in the drop-down list, `refresh()` which is called every tick to update the animation (default tick
rate is 60 ticks per second), and `reset()` which is called whenever the animation is selected from the list or the reload key is pressed.
Finally, you must add your animation to the list in the `loadAnimations()` method, located in the `com.techjar.ledcm.LEDCube` class.

## Android App
There is an Android app to control the LED Cube Manager, located at https://github.com/Techjar/LEDCubeRemote