package mishanesterenko.changevisualizer.wizard;

import org.eclipse.osgi.util.NLS;

/**
 * @author Michael Nesterenko
 *
 */
public class WizardMessages extends NLS {
    private static final String BUNDLE_NAME = "mishanesterenko.changevisualizer.wizard.messages"; //$NON-NLS-1$
    public static String ProjectBasicSettingsPage_can_not_create_project;
    public static String SvnWizard_location_page_description;
    public static String SvnWizard_location_page_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
    }

    private WizardMessages() {
    }
}
