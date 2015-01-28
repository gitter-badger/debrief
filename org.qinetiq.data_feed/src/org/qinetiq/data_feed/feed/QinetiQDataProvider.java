/**
 * 
 */
package org.qinetiq.data_feed.feed;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Date;

import org.mwc.debrief.data_feed.views.LiveFeedViewer;
import org.mwc.debrief.data_feed.views.RealTimeProvider;

import Debrief.ReaderWriter.Replay.ImportReplay;
import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;
import MWC.TacticalData.Fix;
import MWC.TacticalData.Track;
import MWC.Utilities.Timer.TimerListener;

/**
 * @author ian.mayo
 *
 */
public class QinetiQDataProvider implements RealTimeProvider, TimerListener
{

	/**
	 * the automatic timer we are using
	 */
	private MWC.Utilities.Timer.Timer _theTimer;
	
	/** the feed we're listening to
	 * 
	 */
	private LiveFeedViewer _myHost;

	private HiResDate _dtg;

	private WorldLocation _lastLoc;

	private ImportReplay _iff;

	private TrackWrapper _trk;

	public QinetiQDataProvider()
  {

		/** DUMMY the timer-related settings
		 */
		_theTimer = new MWC.Utilities.Timer.Timer();
		_theTimer.stop();
		_theTimer.setDelay(1000);
		_theTimer.addTimerListener(this);  	
  }
	
	/** connect to our data feed
	 * @param host the UI that wants to hear the data
	 */
	public void connect(LiveFeedViewer host)
	{
		_myHost = host;

		_myHost.showMessage("Connecting to 2076");
		
		// ok, start the trigger
		startPlaying();
		
		// and update our state
		_myHost.showState("Connected");
		_myHost.showMessage("Connected to 2076");

	}

	/** disconnect from the data-feed
	 * @param host the viewer that's asking us to disconnect
	 */
	public void disconnect(LiveFeedViewer host)
	{
		if(_myHost != null)
		{
			
		_myHost.showMessage("Disconnecting");
		
		// ok, start the trigger
		stopPlaying();
		
		// and update our state
		_myHost.showState("Disconnected");		
		_myHost.showMessage("Disconnected");

		// done.
		_myHost = null;
		}
		
	}

	/**
	 * @return the name of this provider (shown in drop-down list)
	 */
	public String getName()
	{
		return "QinetiQ provider";
	}
	

	/** timed event happened - produce an artificial fix
	 * 
	 * @param event
	 */
	public void onTime(ActionEvent event)
	{
		
		// the remainder of this function has been commented out, since we no longer
		// receive a copy of text-to-export
		
		
		// ok,  fire the new data event
		
		if(_dtg == null)
		{
			_dtg = new HiResDate(new Date());
			_lastLoc = new WorldLocation(50.65, -1.1, 0);
			_iff = new ImportReplay(){

				@Override
				protected void addThisToExport(String txt)
				{
					newMessage(txt);
				}};
			_trk = new TrackWrapper();
			_trk.setName("Dummy Track");
			_trk.setColor(Color.RED);
		}
		else
		{
			// ok, move forward
			_dtg = new HiResDate((_dtg.getMicros() / 1000) + 60000);
			WorldVector newVec = new WorldVector(1 + Math.random() * 0.5, 
					new WorldDistance(12, WorldDistance.METRES), 
					new WorldDistance(300, WorldDistance.METRES));
			_lastLoc = new WorldLocation(_lastLoc.add(newVec));
		}			
				
		Fix fix = new Fix(_dtg, _lastLoc, 22, 12);
		FixWrapper fw = new FixWrapper(fix);
		fw.setTrackWrapper(_trk);
		
		// ok, now export it
		_iff.exportThis(fw);
		
	}	
	

	protected void newMessage(String txt)
	{
		if (_myHost != null)
		{
			_myHost.insertData(txt);
			_myHost.showMessage("DATA RX");
		}
	}

	/**
	 * ok, start auto-stepping forward through the serial
	 */
	private void startPlaying()
	{
		_theTimer.start();
	}	
	

	/** yes, you guessed it. stop stepping forward through the serial
	 * 
	 *
	 */
	protected void stopPlaying()
	{
		_theTimer.stop();
	}
	

}
