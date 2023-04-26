package main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import ast.AST;
import generacionDeCodigo.GeneracionDeCodigo;
import semantico.AnalisisSemantico;
import sintactico.Parser;
import sintactico.Yylex;
import visitor.ASTPrinter;

/**
 * Clase que inicia el compilador e invoca a todas sus fases.
 * 
 * No es necesario modificar este fichero. En su lugar hay que modificar:
 * 
 * - Para An�lisis Sint�ctico: 
 * 		'sintactico/sintac.y'
 * 		'sintactico/lexico.l'
 * 
 * - Para An�lisis Sem�ntico: 
 * 		'semantico/Identificacion.java'
 * 		'semantico/ComprobacionDeTipos.java'
 * 
 * - Para Generaci�n de C�digo: 
 * 		'generacionDeCodigo/GestionDeMemoria.java'
 * 		'generacionDeCodigo/SeleccionDeInstrucciones.java'
 *
 * @author Jos� Antonio Garc�a Garc�a
 * 
 */
public class Main {

	public static final String programa = "Archivos_Entrada/ExamenEjercicio2_Valido.txt"; // Entrada a usar durante el desarrollo
	public static String fechaEjecucion;

	public static void main(String[] args) throws Exception {
		GestorErrores gestor = new GestorErrores();
		fechaEjecucion = getNombreArchivoSalida();

		AST raiz = compile(programa, gestor); // Poner args[0] en vez de "programa" en la versi�n final
		if (!gestor.hayErrores())
			System.out.println("El programa se ha compilado correctamente.");

		ASTPrinter.toHtml(programa, raiz, "Archivos_Salida/Traza arbol - " + fechaEjecucion);
	}

	/**
	 * Método que coordina todas las fases del compilador
	 */
	public static AST compile(String sourceName, GestorErrores gestor) throws Exception {

		// 1. Fases de An�lisis L�ico y Sint�ctico
		Yylex lexico = new Yylex(new FileReader(sourceName), gestor);
		Parser sintactico = new Parser(lexico, gestor, false);
		sintactico.parse();

		AST raiz = sintactico.getAST();
		if (raiz == null) // Hay errores o el AST no se ha implementado a�n
			return null;

		// 2. Fase de An�lisis Sem�ntico
		AnalisisSemantico semantico = new AnalisisSemantico(gestor);
		semantico.analiza(raiz);
		if (gestor.hayErrores())
			return raiz;

		// 3. Fase de Generaci�n de C�digo
		File sourceFile = new File(sourceName);
		String salida = "Archivos_Salida/salida - " + fechaEjecucion + ".txt";
		Writer out = new FileWriter(new File(salida));

		GeneracionDeCodigo generador = new GeneracionDeCodigo();
		generador.genera(sourceFile.getName(), raiz, out);
		out.close();

		return raiz;
	}

	private static String getNombreArchivoSalida() {
		String time = new SimpleDateFormat("HH-mm-ss-dd-MM-yyyy").format(new Date());
		String[] sTime = time.split("-");
		StringBuilder aDevolver = new StringBuilder("");
		aDevolver.append(sTime[0] + "h_");
		aDevolver.append(sTime[1] + "m_");
		aDevolver.append(sTime[2] + "s - ");
		aDevolver.append(sTime[3] + "d_");
		aDevolver.append(sTime[4] + "m_");
		aDevolver.append(sTime[5] + "a");
		return aDevolver.toString();
	}
}
