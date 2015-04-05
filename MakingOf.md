<h2>Introduction</h2>
The goal of this project was to re-create a XIX century phone call experience and esthetics. A user would pick up the receiver, tell "operator" whom to call and a call would be placed via VoIP.

I have used Android-based device, IOIO Board and some wood and plastic-cutting to re-create this device.

<h2>Construction</h2>
<h3>Components</h3>
A small research led me to using following components:

<img src='http://habrastorage.org/storage1/9c3a064a/605e8925/b4eb6784/66f9cc8e.png' align='center' />

<b>Vintage phone</b> for project enclosure. I was very attracted by the esthetics of this old device. Mix of materials, rich textures and shapes certainly add to the experience. I found a reasonably priced candlestick phone and a ringer box on eBay.

<img src='http://habrastorage.org/storage1/ee70f441/e1e51377/f7f4632e/d505c82f.png' />

<b><a href='http://www.archos.com/products/ta/archos_28it/specs.html?country=us&lang=en'>Archos 28</a></b> as a driving device. Archos 28 is a reasonably priced tablet that has all the features I need: 4Gb of internal memory, Wi-Fi, microphone, audio out and 800Mhz CPU.

One might ask: why not use a micro controller and a set of chips? It looked a bit simpler and more efficient to use Archos 28, as it has all components on its board and also comes with OS Android. Since my phone has to work 24/7 it has to remain plugged in all the time, so power consumption is not an issue.

<img src='http://dlnmh9ip6v2uc.cloudfront.net/images/products/10585-01b_i_ma.jpg' />

<b><a href='http://www.sparkfun.com/products/10585'>IOIO Board</a></b> to interact with hardware. IOIO Board is an amazing device: it plugs into Android device via USB. Android device discovers it as an ADB host. There is  <a href='http://codaset.com/ytai/ioio/source'>a nice little API</a> that allows any Android application read line state (either digitally or do analog read) and generate either digital or PWM signal on a line.

One might ask: why not use <a href='http://developer.android.com/guide/topics/usb/adk.html'>Android ADK</a>? Unfortunately, ADK has been added only in Android 2.3. Archos 28 is running 2.2.


<h2>Metal, Wood and Plastic</h2>

I disassembled vintage phone and phone box to clean it. Since the device was manufactured around 1910 it had quite a lot of dirt, dust and old glue in it. I used warm soapy water for wood and <a href='http://en.wikipedia.org/wiki/WD-40'>WD-40</a> for metals parts. Here are all the components laid out:

<img src='http://habrastorage.org/storage1/be4bd337/a7de58fb/e1361c1e/814a4bab.png' />

Although Archos 28 is certainly not intended for amateur tinkering it was not that difficult to wire out power button, microphone, audio out and Wi-Fi antenna. Candlestick holder of and old devices was also very simple to work with: it had a simple mechanism that would bring a line to the ground if the telephone is on hook. Here is how my devices looked like:

<img src='http://habrastorage.org/storage1/b2d37208/e8a37707/3e9a51fe/21aeeabb.png' />

Archos 28 didn't fit the old ringer box very well. To be exact it was USB cable to was sticking out and preventing device to fit in. I used <a href='http://en.wikipedia.org/wiki/Dremel'>Dremel tool</a> to fix this. I cut a small slope to make sure USB cable fits in nicely.
I have also cut out holes for wires and installed couple hinges:

<img src='http://habrastorage.org/storage1/641f3bca/9a8926e1/180f0877/ca8ea940.png' />

Somewhere along the process I was surprised to discover that my table resembled that of Time Traveler, who has just returned to his XIX-st century from our XII-st:

<img src='http://habrastorage.org/storage1/c31be2ce/742264a7/757c1c4f/4dba7490.png' />

I really wanted to keep authentic vintage ringer. Unfortunately, I couldn't leverage any of the original designs: old electromagnets took up all space in the finder box. So, I  had to throw everything away and design ringer mechanism from scratch. I mounted original metal ball (that hits the chimes) on a pivoting platform that it driven by a small servo motor. The motor has an ellipse on its axis. This ellipse rotates and pivots the platform. Here is how it looks on a diagram:

<img src='http://habrastorage.org/storage1/50d49e54/7eb5edac/dcf4af53/5ab50df8.png' />

I used <a href='http://inkscape.org/'>Inkscape</a> to create 2D design. I must mention that using this tool very much feels like a torture. I really hope I'll be able to find better affordable tools for my next project.

One the design was ready, I've submitted it to <a href='http://www.ponoko.com/'>Ponoko</a> for laster cutting. Laser-cut panel arrived couple weeks later:

<img src='http://habrastorage.org/storage1/5b85e42e/114ebf1a/93d9c2ca/ab1b1a51.png' />

Add some glue and these components transform into a ringer mechanism:

<img src='http://habrastorage.org/storage1/f1082eb3/97a4d9cf/db5f8fb8/2d7e8309.png' />

<h2>Circuit Assembly</h2>

There was really not much to the device's circuit. Archos 28 was connected to IOIO Board via USB. The IOIO Board was connected to candlestick "on/off hook" line directly. It was also connected to the servo motor via <a href='http://www.st.com/internet/com/TECHNICAL_RESOURCES/TECHNICAL_LITERATURE/DATASHEET/CD00002269.pdf'>TS1220-600T</a>. Here is how it looked:

<img src='http://habrastorage.org/storage1/5356209d/99c5f2a7/771f0602/fc2f930e.png' />

One can imagine, how it fit into the enclosure:

<img src='http://habrastorage.org/storage1/659b3d6f/c33c7180/4b46472f/0deb80ca.png' />

Once the circuit was in the ringer box Wi-Fi quality reduced significantly. So, I had to purchase and attach an external Wi-Fi antenna. Archos 28 happens to have a very well defined UF.L port. Here is how the system fit into the ringer box:

<img src='http://habrastorage.org/storage1/8d3c87a6/52a2c403/e09227f7/5a80b93a.png' />

Here is how wires looked like:

<img src='http://habrastorage.org/storage1/c15ce5e4/1fe0c116/c08e481e/6bd62638.png' />

<h2>Software</h2>
<h3>Primary Components</h3>

<b><a href='http://cmusphinx.sourceforge.net/'>CMU Sphinx</a></b> is an open source voice recognition project maintained by Carnegie Mellon. The system consists of two parts: recognizer code and files with voice model and language model. It was easy to compile library code for Android. There is a great <a href='http://cmusphinx.sourceforge.net/2011/05/building-pocketsphinx-on-android/comment-page-1/'>example</a> posted by CMU Sphinx's creators.
One can teach CMU Sphinx their own pronunciation. All one has to do is to record 20 sentences and run generated files thought a supplied tool. This can significantly increase recognition quality. What is more, one can build a language model. This would basically tell recognizer what words and phrases to expect. In my case a primary phrase was "call <i>name</i>", where <i>name</i> is one of the names from my address book. Having such model also increases recognition quality.

One might ask: why not use Google Voice? Unfortunately, it is really bad at understanding my pronunciation. And it also not so good at recognizing names.

One might ask: why not use special micro controller? I have certainly considered this approach. One solution I found was <a href='http://www.sensoryinc.com/'>Sensory</a>. Unfortunately, it looked too expensive. Well, it seemed like I would have to do the same amount of work, as with CMU Sphinx and it will result in comparable quality, but I would have to pay for the chip.

"<b>No speech generator</b>" – I was very convinced in this after trying several different generators. All text-to-speech engines created a very un-natural voice. So, I had to ask a human to record all phrases that my phone can possibly tell. What is more, I had her read each phrase several time. During playback I pick a random version of the phrase; this creates a strong illusion of a real human on the other end.

<b><a href='http://www.pjsip.org/'>PJSIP</a></b> – is an open-source implementation of the SIP stack. In other words, it is open VoIP library. I didn't have much trouble with it: downloaded, compiled and used it. <a href='http://code.google.com/p/csipsimple/'>CSipSimple</a> is a big project open source that also uses it. This project very helpful, as it contained some great usage examples.

One might ask: why not use Skype? This was my original idea. I've subscribed to Skype Developer Program. Unfortunately reading license agreement revealed that Skype SDK can not be installed on any devices controlled by Android.

One might ask: why not SIP stack that is built into Android? Unfortunately, the stack has been added only in Android 2.3. Archos 28 is running 2.2.

<h3>Workflow</h3>
When telephone is off the hook:
<ol>
<blockquote><li>Wait one second</li>
<li>Say "Number, please!"</li>
<li>Start voice recognition</li>
<li>If recognized "call <i>name</i>", go to next, otherwise say "Sorry, I didn't get that" and go to 3</li>
<li>Say "Calling <i>name</i>..."</li>
<li>Start voice recognition</li>
<li>If recognized "no" or  "stop" go to 2, otherwise go to next</li>
<li>Place a VoIP call</li>
<li>Say "Call placed"</li>
<li>Wait until the call is terminated</li>
<li>Say "Call terminated"</li>
</ol></blockquote>

When incoming call is received ring the bell and wait until either telephone is picked up, or other end terminate a call or 20 seconds pass. Ring the bell with one second intervals.

<h3>Android App Format</h3>
Phone application is actually a background service. There is also a light-wait user application that displays current status. The services starts on app startup or on user app launch.