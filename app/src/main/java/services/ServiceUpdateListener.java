package services;

/**
 * @author - Matias Grioni
 * @created - 12/30/15
 *
 * An interface for when a {@code Service} is executed. The update method will be called when the
 * {@code Service} receives the appropriate broadcast and {@code Service#onStartCommand} is called.
 */
public interface ServiceUpdateListener {
    void onUpdate();
}
