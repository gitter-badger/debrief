/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package org.mwc.debrief.core.ContextOperations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.operations.CMAPOperation;
import org.mwc.cmap.core.property_support.RightClickSupport.RightClickContextItemGenerator;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.TrackWrapper;
import Debrief.Wrappers.Track.TrackSegment;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldSpeed;
import MWC.GenericData.WorldVector;

/**
 * @author ian.mayo
 */
public class RemoveTrackJumps implements RightClickContextItemGenerator
{

	/**
	 * @param parent
	 * @param theLayers
	 * @param parentLayers
	 * @param subjects
	 */
	public void generate(final IMenuManager parent, final Layers theLayers,
			final Layer[] parentLayers, final Editable[] subjects)
	{
		// we're only going to work with one item
		Collection<Editable> points = null;
		String tmpTitle = null;
		if (subjects.length == 1)
		{
			// is it a track?
			final Editable thisE = subjects[0];
			if (thisE instanceof TrackSegment)
			{
				TrackSegment ts = (TrackSegment) thisE;
				points = ts.getData();
				tmpTitle = "Remove jumps for selected track";
			}
		}
		else
		{
			// see if it's a series of points
			final Editable thisE = subjects[0];
			if (thisE instanceof FixWrapper)
			{
				// collate the points into a collection
				points = new Vector<Editable>();

				// loop through the data
				for (int i = 0; i < subjects.length; i++)
				{
					Editable editable = subjects[i];
					points.add(editable);
				}

				tmpTitle = "Remove jumps in selected positions";
			}
		}

		// ok, is it worth going for?
		if (points != null)
		{
			final String title = tmpTitle;
			final Collection<Editable> permPoints = points;

			// right,stick in a separator
			parent.add(new Separator());

			final TrackWrapper track = (TrackWrapper) parentLayers[0];

			// and the new drop-down list of interpolation frequencies

			// yes, create the action
			final Action convertToTrack = new Action(title)
			{
				public void run()
				{
					// ok, go for it.
					// sort it out as an operation
					final IUndoableOperation removeJumps = new RemoveJumps(title,
							theLayers, track, permPoints);

					// ok, stick it on the buffer
					CorePlugin.run(removeJumps);
				}

			};
			parent.add(convertToTrack);
		}

	}

	static class RemoveJumps extends CMAPOperation
	{

		/**
		 * the parent to update on completion
		 */
		private final Layers _layers;

		/**
		 * list of new fixes we're creating
		 */
		private Collection<Editable> _points;

		/**
		 * the track we're interpolating
		 */
		private final TrackWrapper _track;

		private HashMap<FixWrapper, WorldLocation> _newFixes;

		public RemoveJumps(String title, Layers theLayers, TrackWrapper track,
				Collection<Editable> points)
		{
			super(title);
			_layers = theLayers;
			_track = track;
			_points = points;
		}

		static class Leg
		{
			final long startTime;
			final long endTime;
			final WorldVector offset;

			public Leg(FixWrapper startP, FixWrapper jumpP, FixWrapper lockP)
			{
				startTime = startP.getTime().getDate().getTime();
				endTime = lockP.getTime().getDate().getTime();

				// ok, calculate the offset
				offset = lockP.getLocation().subtract(jumpP.getLocation());
			}

			public boolean contains(long theTime)
			{
				boolean res = ((theTime >= startTime) && (theTime <= endTime));

				return res;
			}

			public WorldVector offsetFor(FixWrapper thisP)
			{
				WorldVector res = null;
				long thisT = thisP.getDateTimeGroup().getDate().getTime();

				// just check this isn't the start or end time
				if ((thisT != startTime) && (thisT != endTime))
				{
					// ok, how far along time period are we
					double tDelta = thisT - startTime;
					double proportion = tDelta / (endTime - startTime);
					double newDistance = offset.getRange()
							* proportion;

					System.out.println("td:" + (long)tDelta + " prop:"+ proportion + " newD:" + newDistance);
					
					// generate the offset
					res = new WorldVector(offset.getBearing(), newDistance, 0);
				}

				return res;
			}
		}

		public IStatus execute(final IProgressMonitor monitor, final IAdaptable info)
				throws ExecutionException
		{
			// get ready to store the old positions
			_newFixes = new HashMap<FixWrapper, WorldLocation>();

			// PART ONE - FIND THE JUMPS
			ArrayList<Leg> legs = getLegs(_points);

			// did we find any?
			applyOffsets(legs, _points, _newFixes);

			// sorted, do the update
			if (_layers != null)
				_layers.fireModified(_track);

			return Status.OK_STATUS;
		}

		static void applyOffsets(ArrayList<Leg> legs, Collection<Editable> points,
				HashMap<FixWrapper, WorldLocation> fixes)
		{
			// now pass through again, applying the offset
			Iterator<Editable> iter = points.iterator();
			while (iter.hasNext())
			{
				FixWrapper thisP = (FixWrapper) iter.next();

				// find the correct leg
				Leg relevantLeg = findLegFor(thisP, legs);

				// is it in a leg?
				if (relevantLeg != null)
				{
					// ok, find the offset vector
					WorldVector offset = relevantLeg.offsetFor(thisP);
					
					if (offset != null)
					{
						// store the old existing location
						fixes.put(thisP, thisP.getLocation());

						// and apply the new offset
						thisP.setLocation(thisP.getLocation().add(offset));
					}
				}
			}
		}

		static Leg findLegFor(FixWrapper thisP, ArrayList<Leg> legs)
		{
			HiResDate theTime = thisP.getTime();

			Leg res = null;

			// loop through the legs
			for (Iterator<Leg> iterator = legs.iterator(); iterator.hasNext();)
			{
				Leg leg = (Leg) iterator.next();

				if (leg.contains(theTime.getDate().getTime()))
				{
					res = leg;
					break;
				}
			}
			return res;
		}

		static ArrayList<Leg> getLegs(Collection<Editable> points)
		{
			WorldSpeed THRESHOLD_SPEED = new WorldSpeed(16, WorldSpeed.Kts);

			ArrayList<Leg> legs = new ArrayList<Leg>();

			FixWrapper startP, jumpP, lockP;

			// get the points
			Iterator<Editable> iter = points.iterator();

			// store the first point as a start
			startP = (FixWrapper) iter.next();

			// remember the previous position
			FixWrapper prev = startP;

			while (iter.hasNext())
			{
				FixWrapper fix = (FixWrapper) iter.next();

				// ok, what's the distance from the previous position
				double locDeltaDegs = fix.getLocation().rangeFrom(prev.getLocation());
				WorldDistance delta = new WorldDistance(locDeltaDegs,
						WorldDistance.DEGS);

				// and how long did it take?
				double timeDeltaMillis = fix.getDateTimeGroup().getDate().getTime()
						- prev.getDateTimeGroup().getDate().getTime();
				double timeDeltaHours = timeDeltaMillis / 1000 / 60 / 60d;

				// what's the effective speed
				WorldSpeed speedTravelled = new WorldSpeed(
						delta.getValueIn(WorldDistance.MINUTES) / timeDeltaHours,
						WorldSpeed.Kts);

				// is this so fast that it can only be a jump?
				if (speedTravelled.greaterThan(THRESHOLD_SPEED))
				{
					// ok, we've found a jump
					jumpP = prev;
					lockP = fix;
					legs.add(new Leg(startP, jumpP, lockP));

					// ok, the lock point becomes the first point of the next leg
					startP = lockP;
				}
				// ok, now move along the bed
				prev = fix;
			}

			return legs;
		}

		public IStatus undo(final IProgressMonitor monitor, final IAdaptable info)
				throws ExecutionException
		{
			// loop through the positions
			for (Iterator<Editable> iterator = _points.iterator(); iterator.hasNext();)
			{
				FixWrapper fix = (FixWrapper) iterator.next();
				// get this location
				WorldLocation loc = _newFixes.get(fix);

				// was an offset applied?
				if (loc != null)
				{
					// put the location back in
					fix.setLocation(loc);
				}
			}

			// and clear the new tracks item
			_newFixes.clear();
			_newFixes = null;
			_layers.fireModified(_track);

			return Status.OK_STATUS;
		}
	}

}
