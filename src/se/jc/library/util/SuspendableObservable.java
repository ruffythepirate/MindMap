/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.util;

import java.util.BitSet;
import java.util.Observable;

/**
 *
 * @author Ruffy
 */
public class SuspendableObservable extends Observable {

    private boolean _needToNotify;
    private boolean _suspended;
    private BitSet _updatedFields;

    @Override
    public void notifyObservers() {
        if (!_suspended) {
            super.notifyObservers(getUpdatedFields());
            getUpdatedFields().clear();
            _needToNotify = false;
        } else {
            _needToNotify = true;
        }
    }

    public void notifyObservers(int changedData) {
        getUpdatedFields().set(changedData);
        notifyObservers();
    }

    public void setChangeAndNotifyObservers(int changeBit) {
        setChanged();
        notifyObservers(changeBit);
    }

    public void suspendBinding() {
        _suspended = true;
    }

    public void resumeBinding() {
        _suspended = false;
        if (_needToNotify) {
            notifyObservers();
        }
    }

    protected BitSet getUpdatedFields() {
        if(_updatedFields == null)
        {
            _updatedFields = new BitSet();
        }
        return _updatedFields;
    }
}
