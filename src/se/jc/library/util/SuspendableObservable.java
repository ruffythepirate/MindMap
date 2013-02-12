/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.util;

import java.util.Observable;

/**
 *
 * @author Ruffy
 */
public class SuspendableObservable extends Observable {

    private boolean _needToNotify;
    private boolean _suspended;

    @Override
    public void notifyObservers() {
        notifyObservers(null);
    }

    @Override
    public void notifyObservers(Object data) {

        if (!_suspended) {
            super.notifyObservers(data);
            _needToNotify = false;
        } else {
            _needToNotify = true;
        }
    }

    public void setChangeAndNotifyObservers(Object data) {
        setChanged();
        notifyObservers(data);
    }

    public void suspendBinding() {
        _suspended = true;
    }

    public void resumeBinding(Object data) {
        _suspended = false;
        if (_needToNotify) {
            notifyObservers(data);
        }
    }
}
