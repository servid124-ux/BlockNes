package net.BlockNes.event;

public class TranslationContainer extends TextContainer {
    private String[] parameters;

    public TranslationContainer(String text, String... parameters) {
        super(text);
        this.parameters = parameters == null ? new String[0] : parameters.clone();
    }

    public String[] getParameters() { return parameters.clone(); }

    public String getParameter(int i) { return parameters[i]; }

    public void setParameters(String... p) { parameters = p == null ? new String[0] : p.clone(); }
}
