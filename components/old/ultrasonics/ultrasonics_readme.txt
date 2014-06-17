Ultrasonics How-To
-------------------
Paul Duff <duff@cs.bris.ac.uk>
Jan Humble <jch@cs.nott.ac.uk>
Stefan Rennick <sre@cs.nott.ac.uk>



Technical Details
------------------

http://www.cs.bris.ac.uk/home/duff/ultrasonics.html 

We are currently using 19200 baud, though 9600 is a possibility if you have an older ultrasonics box (though this seems unlikely).  Other settings: 8 data bits, 1 stop bit, no parity, RTS/CTS handshaking disabled should work. 

The only job of the ultrasonics box is to send the relative times at which chirps reach it.  It sits and waits until a chirp is detected reaching one of the receivers, calls this time "0", and then starts a timer.  As the chirp arrives at each other receiver, the time is noted.  If a chirp doesn't reach a particular receiver, it will time out and give an "infinite" reading.

Once all receivers have either been timed or have timed out, the box sends a delimiter followed by the six relative times.  One of the times will be zero, and the others some greater value.  The format of the buffer is actually '$U' followed by eight pairs of bytes, but the last two pairs of bytes are ignored in a six receiver system.

The conversion of the byte pairs into relative distances is as below - basically, it converts a clock count into a time, then multiplies by the speed of sound to give a relative distance.  The "* 100.0" factor at the end is to convert into centimetres:

rel_distance = (byte0 + byte1*256.0) / 2500000.0 * 343.0 * 100.0

Once the relative distances have been read, the tracking algorithm works out the additional offset needed to convert them into absolute distances, and multilaterates to find the transmitting tag's position. 


Installation & Calibration
---------------------------

The units for positioning are centimetres.  The co-ordinate system you use can be chosen abitrarily - decide a convenient place for the origin and directions for the X, Y and Z axes, and then measure the receiver positions within this system.  It's important that the co-ordinates are input in the right order into the program---seems obvious but easy to get wrong!

The file format usually contains one line per receiver, where each line is a space-separated list of x, y and z co-ordinates in centimetres.  The first line of the file may be a number indicating the number of receivers in the system.  For example, for 4 receivers the file might look as follows:

4
0.0   0.0   0.0
100.0   0.0   0.0
50.3   75.0   10.5
100.0   90.0   10.0

Also, make sure that the receiver microphones point directly toward the area you intend to perform positioning within---this improves the reliability of the receivers.  The transmitting tags also need to point in the general direction of the receivers, and it's best to attach them somewhere where they won't be occluded too often.

In order for a chirper to be located, a minimum of four receivers must be visible.  This is because the system has to find four unknowns - x, y, z and the distance offset between the chirper and the nearest receiver.  Having five or six receivers visible improves the reliability of tracking, in case one or two receivers fail to detect the chirper.

As far as receiver placement goes, we would recommend concentrating receivers around the areas you expect will need most coverage.  This should help to minimize the number of missed readings that occur. 