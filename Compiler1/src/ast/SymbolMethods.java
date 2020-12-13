package ast;

import java.util.List;

public class SymbolMethods extends SymbolDetails {

	private List<String> parameters;
	
	public SymbolMethods(String type, List<String> params) {
		super(type);
		this.setParameters(params);
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

}
