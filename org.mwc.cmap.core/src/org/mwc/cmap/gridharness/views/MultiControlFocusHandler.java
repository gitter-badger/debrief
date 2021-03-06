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
package org.mwc.cmap.gridharness.views;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * This is a hack that allows to check whether focus is transferred between
 * different parts of the multi-control composite.
 * <p>
 * Implementation relies on the fact that focusLost and focusGained are called
 * in the single command, and fires async command that is cancelled if
 * focusGained is called for one of the different parts before the command have
 * been started.
 */
public abstract class MultiControlFocusHandler implements FocusListener {

	private CancellableRunnable myDeferredDeactivate;

	/**
	 * In contrast to {@link FocusListener#focusLost(FocusEvent)}, this method
	 * is called only if the focus is NOT transferred between parts of
	 * multi-control figure specified on construction time.
	 */
	protected abstract void focusReallyLost(FocusEvent e);

	public MultiControlFocusHandler(final Control... parts) {
		for (final Control next : parts) {
			next.addFocusListener(this);
		}
	}

	public void focusGained(final FocusEvent e) {
		if (myDeferredDeactivate != null) {
			myDeferredDeactivate.cancel();
		}
		myDeferredDeactivate = null;
	}

	public void focusLost(final FocusEvent e) {
		myDeferredDeactivate = new CancellableRunnable() {

			@Override
			public void doRun() {
				focusReallyLost(e);
			}
		};
		Display.getCurrent().asyncExec(myDeferredDeactivate);
	}

	private static abstract class CancellableRunnable implements Runnable {

		public boolean myIsCancelled;

		public void cancel() {
			myIsCancelled = true;
		}

		public final void run() {
			if (!myIsCancelled) {
				doRun();
			}
		}

		public abstract void doRun();
	}

}
