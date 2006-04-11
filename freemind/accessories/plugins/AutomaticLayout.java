/*
 * Created on 16.03.2004
 *
 */
package accessories.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import accessories.plugins.dialogs.ChooseFormatPopupDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import freemind.common.PropertyBean;
import freemind.common.PropertyControl;
import freemind.common.SeparatorProperty;
import freemind.common.XmlBindingTools;
import freemind.common.PropertyControl.TextTranslator;
import freemind.controller.Controller;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.Patterns;
import freemind.extensions.HookRegistration;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.StylePatternFactory;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.hooks.PermanentMindMapNodeHookAdapter;
import freemind.preferences.FreemindPropertyContributor;
import freemind.preferences.FreemindPropertyListener;
import freemind.preferences.layout.OptionPanel;

/**
 * @author foltin
 * 
 */
public class AutomaticLayout extends PermanentMindMapNodeHookAdapter {

    private static final String AUTOMATIC_FORMAT_LEVEL = "automaticFormat_level";

    /**
     * Registers the property pages.
     * 
     * @author foltin
     * 
     */
    public static class Registration implements HookRegistration {
        private AutomaticLayoutPropertyContributor mAutomaticLayoutPropertyContributor;

        private final MindMapController modeController;

        private static FreemindPropertyListener listener = null;

        public Registration(ModeController controller, MindMap map) {
            modeController = (MindMapController) controller;
        }

        public void register() {
            // add listener:
            if (listener == null) {
                FreemindPropertyListener listener = new FreemindPropertyListener() {

                    public void propertyChanged(String propertyName,
                            String newValue, String oldValue) {
                        if (propertyName.startsWith(AUTOMATIC_FORMAT_LEVEL)) {
                            patterns = null;
                        }
                    }
                };
                Controller.addPropertyChangeListener(listener);
            }

            mAutomaticLayoutPropertyContributor = new AutomaticLayoutPropertyContributor(
                    modeController);
            OptionPanel.addContributor(mAutomaticLayoutPropertyContributor);
        }

        public void deRegister() {
            OptionPanel.removeContributor(mAutomaticLayoutPropertyContributor);
        }

    }
    
    /**
     * Translates style pattern properties into strings. 
     * */
    static class StylePropertyTranslator implements TextTranslator {
        private final MindMapController controller;
        
        StylePropertyTranslator(MindMapController controller) {
            super();
            this.controller = controller;
        }
        
        public String getText(String pKey) {
            return controller.getText(pKey);
        }
    }
    

    /** Currently not used.
     *  Is useful if you want to make single patterns changeable.
     *  */
    public static class StylePatternProperty extends PropertyBean implements
            PropertyControl, ActionListener {

        String description;

        String label;

        String pattern;

        JButton mButton;

        private final TextTranslator mTranslator;

        private final MindMapController mindMapController;

        public StylePatternProperty(String description, String label,
                TextTranslator pTranslator, MindMapController pController) {
            super();
            this.description = description;
            this.label = label;
            mTranslator = pTranslator;
            mindMapController = pController;
            mButton = new JButton();
            mButton.addActionListener(this);
            pattern = null;
        }

        public String getDescription() {
            return description;
        }

        public String getLabel() {
            return label;
        }

        public void setValue(String value) {
            pattern = value;
            Pattern resultPattern = getPatternFromString();
            String patternString = StylePatternFactory.toString(resultPattern,
                    new StylePropertyTranslator(mindMapController));
            mButton.setText(patternString);
            mButton.setToolTipText(patternString);
        }

        public String getValue() {
            return pattern;
        }

        public void layout(DefaultFormBuilder builder,
                TextTranslator pTranslator) {
            JLabel label = builder.append(pTranslator.getText(getLabel()),
                    mButton);
            label.setToolTipText(pTranslator.getText(getDescription()));
            // add "reset to standard" popup:

        }

        public void actionPerformed(ActionEvent arg0) {
            // construct pattern:
            Pattern pat = getPatternFromString();
            ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(
                    mindMapController.getFrame().getJFrame(),
                    mindMapController,
                    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle",
                    pat);
            formatDialog.setModal(true);
            formatDialog.pack();
            formatDialog.setVisible(true);
            // process result:
            if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
                Pattern resultPattern = formatDialog.getPattern();
                resultPattern.setName("dummy");
                pattern = XmlBindingTools.getInstance().marshall(resultPattern);
                setValue(pattern);
                firePropertyChangeEvent();
            }
        }

        private Pattern getPatternFromString() {
            return StylePatternFactory.getPatternFromString(pattern);
        }

        public void setEnabled(boolean pEnabled) {
            mButton.setEnabled(pEnabled);
        }

    }

    public static class StylePatternListProperty extends PropertyBean implements
            PropertyControl, ListSelectionListener {

        String description;

        String label;

        String patterns;

        JList mList;

        private final TextTranslator mTranslator;

        private final MindMapController mindMapController;

        private DefaultListModel mDefaultListModel;

        public StylePatternListProperty(String description, String label,
                TextTranslator pTranslator, MindMapController pController) {
            super();
            this.description = description;
            this.label = label;
            mTranslator = pTranslator;
            mindMapController = pController;
            mList = new JList();
            mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            mDefaultListModel = new DefaultListModel();
            mList.setModel(mDefaultListModel);
            mList.addListSelectionListener(this);
            patterns = null;
        }

        public String getDescription() {
            return description;
        }

        public String getLabel() {
            return label;
        }

        public void setValue(String value) {
            patterns = value;
            Patterns resultPatterns = getPatternsFromString();
            mDefaultListModel.clear();
            int j = 1;
            StylePropertyTranslator stylePropertyTranslator = new StylePropertyTranslator(mindMapController);
            for (Iterator i = resultPatterns.getListChoiceList().iterator(); i.hasNext();) {    
                Pattern pattern = (Pattern) i.next();
                mDefaultListModel.addElement(mTranslator.getText("level"+j) + ": " + StylePatternFactory.toString(pattern, stylePropertyTranslator));
                j++;
            }
        }

        public String getValue() {
            return patterns;
        }

        public void layout(DefaultFormBuilder builder,
                TextTranslator pTranslator) {
            JLabel label = builder.append(pTranslator.getText(getLabel()),
                    new JScrollPane(mList));
            label.setToolTipText(pTranslator.getText(getDescription()));
        }


        private Patterns getPatternsFromString() {
            return StylePatternFactory.getPatternsFromString(patterns);
        }

        public void setEnabled(boolean pEnabled) {
            mList.setEnabled(pEnabled);
        }

        public void valueChanged(ListSelectionEvent e) {
            // construct pattern:
            Patterns pat = getPatternsFromString();
            JList source = (JList)e.getSource();
            if(source.getSelectedIndex() < 0)
                return;
            Pattern choice = (Pattern) pat.getChoice(source.getSelectedIndex());
            ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(
                    mindMapController.getFrame().getJFrame(),
                    mindMapController,
                    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle",
                    choice);
            formatDialog.setModal(true);
            formatDialog.pack();
            formatDialog.setVisible(true);
            // process result:
            if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
                formatDialog.getPattern(choice);
                patterns = XmlBindingTools.getInstance().marshall(pat);
                setValue(patterns);
                firePropertyChangeEvent();
            }
        }

    }

    private static final class AutomaticLayoutPropertyContributor implements
            FreemindPropertyContributor {

        private final MindMapController modeController;

        public AutomaticLayoutPropertyContributor(
                MindMapController modeController) {
            this.modeController = modeController;
        }

        public List getControls(TextTranslator pTextTranslator) {
            Vector controls = new Vector();
            controls
                    .add(new OptionPanel.NewTabProperty(
                            "accessories/plugins/AutomaticLayout.properties_PatternTabName"));
            controls
                    .add(new SeparatorProperty(
                            "accessories/plugins/AutomaticLayout.properties_PatternSeparatorName"));
            controls.add(new StylePatternListProperty("level",
                    AUTOMATIC_FORMAT_LEVEL, pTextTranslator,
                    modeController));
            return controls;
        }
    }

    private static Patterns patterns = null;

    /**
     * 
     */
    public AutomaticLayout() {
        super();

    }

    private void setStyle(MindMapNode node) {
        logger.finest("updating node id="
                + node.getObjectId(getMindMapController()) + " and text:"
                + node);
        int depth = depth(node);
        logger.finest("COLOR, depth=" + (depth));
        reloadPatterns();
        int myIndex = patterns.sizeChoiceList() - 1;
        if (depth < patterns.sizeChoiceList())
            myIndex = depth;
        Pattern p = (Pattern) patterns.getChoice(myIndex);
        getMindMapController().applyPattern(node, p);
    }

    private int depth(MindMapNode node) {
        if (node.isRoot())
            return 0;
        return depth(node.getParentNode()) + 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemind.extensions.PermanentNodeHook#onAddChild(freemind.modes.MindMapNode)
     */
    public void onAddChildren(MindMapNode newChildNode) {
        logger.finest("onAddChildren " + newChildNode);
        super.onAddChild(newChildNode);
        setStyleRecursive(newChildNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemind.extensions.PermanentNodeHook#onUpdateChildrenHook(freemind.modes.MindMapNode)
     */
    public void onUpdateChildrenHook(MindMapNode updatedNode) {
        super.onUpdateChildrenHook(updatedNode);
        setStyleRecursive(updatedNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemind.extensions.PermanentNodeHook#onUpdateNodeHook()
     */
    public void onUpdateNodeHook() {
        super.onUpdateNodeHook();
        setStyle(getNode());
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode)
     */
    public void invoke(MindMapNode node) {
        super.invoke(node);
        setStyleRecursive(node);
    }

    /** get styles from preferences: */
    private void reloadPatterns() {
        if (patterns == null) {
            String property = getMindMapController().getFrame()
                    .getProperty(AUTOMATIC_FORMAT_LEVEL);
            patterns = StylePatternFactory
                    .getPatternsFromString(property);
        }
    }

    /**
     * @param node
     */
    private void setStyleRecursive(MindMapNode node) {
        logger.finest("setStyle " + node);
        setStyle(node);
        // recurse:
        for (Iterator i = node.childrenUnfolded(); i.hasNext();) {
            MindMapNode child = (MindMapNode) i.next();
            invoke(child);
        }
    }

}