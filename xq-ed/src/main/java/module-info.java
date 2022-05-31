module xqed {
	requires java.base;
	requires javafx.fxml;
	requires javafx.graphics;
	requires transitive javafx.controls;
	requires javafx.base;
	requires transitive org.antlr.antlr4.runtime;
	
	exports xqed;
	exports xqed.gui;
	exports xqed.xiangqi;
}