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
package ASSET.Util.XML.Control.Observers;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import ASSET.Models.Decision.TargetType;
import ASSET.Scenario.Observers.RecordToFileObserverType;
import ASSET.Scenario.Observers.ScenarioObserver;
import ASSET.Scenario.Observers.Recording.CSVTrackObserver;
import ASSET.Util.XML.Decisions.Util.TargetTypeHandler;

abstract class CSVTrackObserverHandler extends CoreFileObserverHandler
{

  private final static String type = "CSVTrackObserver";
	private Boolean _recordDec;
	private Boolean _recordDet;
	private Boolean _recordPos;
	private TargetType _toTrack;

  private static final String RECORD_DETECTIONS = "record_detections";
  private static final String RECORD_DECISIONS = "record_decisions";
  private static final String RECORD_POSITIONS = "record_positions";
  private static final String TARGET_TYPE = "SubjectToTrack";
	
  public CSVTrackObserverHandler()
  {
    super(type);
    
    addAttributeHandler(new HandleBooleanAttribute(RECORD_DETECTIONS)
    {
      public void setValue(String name, final boolean val)
      {
      	_recordDet = val;
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(RECORD_DECISIONS)
    {
      public void setValue(String name, final boolean val)
      {
      	_recordDec = val;
      }
    });
    addAttributeHandler(new HandleBooleanAttribute(RECORD_POSITIONS)
    {
      public void setValue(String name, final boolean val)
      {
      	_recordPos = val;
      }
    });

    addHandler(new TargetTypeHandler(TARGET_TYPE)
    {
      public void setTargetType(TargetType type1)
      {
      	_toTrack = type1;
      }
    });
  }

  public void elementClosed()
  {
    // create ourselves
    final CSVTrackObserver timeO = new CSVTrackObserver(_directory, _fileName, false, _name, _isActive);

    if(_recordDec != null)
    timeO.setRecordDecisions(_recordDec);
    if(_recordDet != null)
    timeO.setRecordDetections(_recordDet);
    if(_recordPos != null)
    timeO.setRecordPositions(_recordPos);
    if(_toTrack != null)
    	timeO.setSubjectToTrack(_toTrack);
    
    _recordDec = null;
    _recordDet = null;
    _recordPos = null;
    _toTrack = null;
    
    setObserver(timeO);

    // and reset
    super.elementClosed();
  }


  abstract public void setObserver(ScenarioObserver obs);

  static public void exportThis(final Object toExport, final org.w3c.dom.Element parent,
                                final org.w3c.dom.Document doc)
  {
    // create ourselves
    final org.w3c.dom.Element thisPart = doc.createElement(type);

    // get data item
    final RecordToFileObserverType bb = (RecordToFileObserverType) toExport;

    // output the parent attrbutes
    CoreFileObserverHandler.exportThis(bb, thisPart);

    // output it's attributes


    // output it's attributes
    parent.appendChild(thisPart);

  }


}