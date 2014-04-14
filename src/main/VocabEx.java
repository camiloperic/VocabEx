package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Scanner;

public class VocabEx {

	private static final String CHAR_ENCONDING="UTF-8";
	private static final String CHAR_ENCONDING_IO="IBM850";
	//Metadata
	private static final int M_FROM = 0;
	private static final int M_TO = 1;
	private static final int M_COMMENT = 2;
	private static final int M_CLASS = 3;
	private static final int M_LASTTEST = 4;
	private static final int M_TESTCOUNT = 5;
	private static final int M_SUCCCOUNT = 6;
	//Command strings
	private static final String RUNTEST_PARAMETER = "runtest";
	private static final String STATS_COMMAND = "stats";
	private static final String ADD_COMMAND = "add";
	private static final String TEST_COMMAND = "test";
	private static final String QUIT_COMMAND = "quit";
	//Warn strings
	private static final String WARN01 = "No vocabulary list file found!";
	private static final String WARN02 = "No configuration file found, using default values!";
	private static final String WARN03 = "Setting word classes to: ";
	private static final String WARN04 = "Setting language FROM to: ";
	private static final String WARN05 = "Setting language TO to: ";
	//Error strings
	private static final String ERROR01 = "Incorrect use! No arguments needed!";
	private static final String ERROR02 = "Invalid command!";
	private static final String ERROR03 = "Problem reading vocabulary file!";
	private static final String ERROR04 = "Problem adding new word, missing parameter(s)!" +
												"\nTry add word_pt word_es!";
	private static final String ERROR05 = "Problem reading the following vocabulary list entry: ";
	private static final String ERROR06 = "Problem creating vocabulary list file!";
	private static final String ERROR07 = "Problem parsing the following date string: ";
	private static final String ERROR08 = "Problem loading the configuration file!";
	//Config
	private static String LANG_FROM = "PT";
		private static final String LANG_FROM_KEY = "langfrom";
	private static String LANG_TO = "ES";
		private static final String LANG_TO_KEY = "langto";
	private static int[] wordClasses = {1,2,3,5,8,13,21,34,55,0};
		private static final String WORD_CLASSES_KEY = "wordclasses";
	private static final String configFilePath_win = "conf\\vocabex.properties";
	private static final String vocFilePath_win = "data\\vocabulary.txt";
	private static final String configFilePath = "conf/vocabex.properties";
	private static final String vocFilePath = "data/vocabulary.txt";
	private static final String dateFormat = "yyyyMMdd";
	private static boolean vocabularyFileExists = false;	
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
	//System
	private static String SYS_OS = System.getProperty("os.name").toLowerCase();
	
	private static ArrayList vocabulary = new ArrayList();
	
	public static void main(String[] args) {
		/**try {
			PrintStream System.out = new PrintStream(System.out, true, CHAR_ENCONDING_IO);
			System.out.println("��������");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}**/
		
		if (args.length == 1) {
			if (args[0].toString().equals(RUNTEST_PARAMETER)) runTest();
		} else if (args.length != 0) {
			System.out.println(ERROR01);
			return;
		}
		readConfigFile();
		String command = "";
		while(!command.toLowerCase().equals(QUIT_COMMAND)) {
			Scanner scanner = new Scanner(System.in);
			System.out.print("VocabEx>");
			command = scanner.nextLine();
			if (command.toLowerCase().equals(ADD_COMMAND)) {
				add();
			} else if (command.toLowerCase().equals(TEST_COMMAND)) {
				test();
			} else if (command.toLowerCase().equals(STATS_COMMAND)) {
				stats();
			} else if (!command.toLowerCase().equals(QUIT_COMMAND) && !command.toLowerCase().equals("")) {
				System.out.println(ERROR02);
			}
		}
		/**
		Scanner scanner = new Scanner(System.in);
		System.out.println("Tell me your name");
		String nome;
		nome = scanner.nextLine();
		scanner.close();
		System.out.println("Bye " + nome);
		**/
	}
	
	private static boolean readVocFile(){
		ArrayList readingVocabulary = new ArrayList();
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(isWindows() ? vocFilePath_win : vocFilePath);
		} catch (FileNotFoundException e) {
			System.out.println(WARN01);
			vocabulary = readingVocabulary;
			return true;
		}
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, CHAR_ENCONDING));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return false;
		}
		String strLine;
		try {
			while ((strLine = br.readLine()) != null) {
				String[] cols = strLine.split("\t");
				if (cols.length != 7) {
					System.out.println(ERROR05);
					System.out.println(strLine);
					continue;
				}
				readingVocabulary.add(new String[]{cols[0], cols[1], cols[2], cols[3], cols[4], cols[5], cols[6]});
			}
			in.close();
		} catch (IOException e) {
			System.out.println(ERROR03);
			return false;
		}
		vocabulary = readingVocabulary;
		return true;
	}
	
	private static boolean readConfigFile() {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(isWindows() ? configFilePath_win : configFilePath);
		} catch (FileNotFoundException e) {
			System.out.println(WARN02);
			return true;
		}
		Properties config = new Properties();
		try {
			config.load(fstream);
		} catch (IOException e) {
			System.out.println(ERROR08);
			return false;
		}
		String configWordClasses = config.getProperty(WORD_CLASSES_KEY);
		if (configWordClasses != null) {
			String[] toInteger = configWordClasses.split(",");
			int[] classes = new int[toInteger.length];
			String toLog = "";
			for (int i = 0; i < classes.length; i++) {
				classes[i] = Integer.parseInt(toInteger[i]);
				toLog += (i != 0 ? ", " : "") + classes[i];
			}
			System.out.println(WARN03+toLog);
			wordClasses = classes;
		}
		LANG_FROM = config.getProperty(LANG_FROM_KEY, LANG_FROM);
		System.out.println(WARN04+LANG_FROM);
		LANG_TO = config.getProperty(LANG_TO_KEY, LANG_TO);
		System.out.println(WARN05+LANG_TO);
		return true;
	}
	
	private static boolean writeVocFile(){
		Writer output;
		boolean emptyList = true;
		try {
			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(isWindows() ? vocFilePath_win : vocFilePath, false), CHAR_ENCONDING));
			for (int i = 0; i < vocabulary.size(); i++) {
				Object[] newEntry = (Object[])vocabulary.get(i);
				output.append((!emptyList ? "\n" : "")+newEntry[0]+"\t"+newEntry[1]+"\t"+newEntry[2]+"\t"+newEntry[3]+"\t"+newEntry[4]+"\t"+newEntry[5]+"\t"+newEntry[6]);
				emptyList = false;
			}
			output.close();
		} catch (IOException e) {
			System.out.println(ERROR03);
			return false;
		}
		return true;
	}
	
	private static boolean addVocabulary(String word_from, String word_to, String comment){
		String pairKey = word_from + "|" + word_to;
		FileInputStream fstream = null;
		int readFileTryCount = 3;
		boolean fileCreated = false;
		while(fstream == null && readFileTryCount>=0) {
			try {
				fstream = new FileInputStream(isWindows() ? vocFilePath_win : vocFilePath);
			} catch (FileNotFoundException e) {
				System.out.println(WARN01);
				File file = new File(isWindows() ? vocFilePath_win : vocFilePath);
				try {
					file.createNewFile();
					fileCreated = true;
				} catch (IOException e1) {
					System.out.println(ERROR06);
				}
				System.out.println("Vocabulary list file successfully created");
			}
			readFileTryCount--;
		}
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in,CHAR_ENCONDING));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return false;
		}
		String strLine;
		try {
			while ((strLine = br.readLine()) != null) {
				String[] cols = strLine.split("\t");
				if (cols.length != 7) {
					System.out.println(ERROR05);
					System.out.println(strLine);
					continue;
				} else if (pairKey.equals(cols[0] + "|" + cols[1])) {
					System.out.println(LANG_FROM+": "+cols[0]+"\t"+LANG_TO+": "+cols[1]+"\talready exists in the list");
					return true;
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println(ERROR03);
			return false;
		}
		Writer output;
		try {
			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(isWindows() ? vocFilePath_win : vocFilePath, true), CHAR_ENCONDING));
			if (!fileCreated) output.append("\n");
			output.append(word_from+"\t"+word_to+"\t"+comment+"\t0\tNULL\t0\t0");
			output.close();
			System.out.println("The pair (["+LANG_FROM+": "+word_from+"]["+LANG_TO+": "+word_to+"]) was successfully added to the vocabulary list");
		} catch (IOException e) {
			System.out.println(ERROR03);
			return false;
		}
		return true;
	}
	
	/**private static String toIO(String text) {
		try {
			byte[] bytes = text.getBytes(CHAR_ENCONDING_IO);
			return new String(bytes, CHAR_ENCONDING_IO);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Problem with character encoding!");
		}
		return "";
	}**/
	
	private static void add() {
		Scanner scanner = new Scanner(System.in);
		Boolean answer = false;
		String lastWord_from = null;
		String lastWord_to = null;
		String lastComment = null;
		while(answer == false) {
			String word_from = "";
			while(word_from.equals("")){
				System.out.print(LANG_FROM+(lastWord_from != null ? "("+lastWord_from+")":"")+": ");
				word_from = scanner.nextLine();
				if (word_from.equals("") && lastWord_from != null) word_from = lastWord_from;
			}
			String word_to = "";
			while(word_to.equals("")) {
				System.out.print(LANG_TO+(lastWord_to != null ? "("+lastWord_to+")":"")+": ");
				word_to = scanner.nextLine();
				if (word_to.equals("") && lastWord_to != null) word_to = lastWord_to;
			}
			System.out.print("COMMENT"+(lastComment != null ? "("+lastComment+")":"")+": ");
			String comment = scanner.nextLine();
			if (comment.equals("") && lastComment != null) comment = lastComment;
			System.out.println(LANG_FROM+": "+word_from+"\t"+LANG_TO+": "+word_to+"\t("+comment+")");
			String rawAnswer = "";
			while (!rawAnswer.equals("y")&&!rawAnswer.equals("n")) {
				System.out.println("Is this correct? (y or n)");
				rawAnswer = scanner.nextLine();
				if (rawAnswer.equals("y")) {
					answer = true;
					addVocabulary(word_from,word_to, comment);
				} else if (rawAnswer.equals("n")) {
					lastWord_from = word_from;
					lastWord_to = word_to;
				}
			}
		}
	}
	
	private static void test() {
		ArrayList toTest = new ArrayList<String[]>();
		ArrayList tested = new ArrayList<String[]>();
		ArrayList notToTest = new ArrayList<String[]>();
		Date now = new Date();
		readVocFile();
		for (Object object:vocabulary){
			Object[] item = (Object[])object;
			if (item[M_LASTTEST].toString().equals("NULL")) {
				toTest.add(item);
			} else if(wordClasses[Integer.parseInt(item[M_CLASS].toString())] == 0) {
				notToTest.add(item);
			} else {
				Date entryDate;
				try {
					entryDate = dateFormatter.parse(item[M_LASTTEST].toString());
				} catch (ParseException e) {
					System.out.println(ERROR07);
					System.out.println(item[M_LASTTEST]);
					continue;
				}
				float lastTested = (now.getTime() - entryDate.getTime())/(1000*60*60*24);
				int interval = wordClasses[Integer.parseInt(item[M_CLASS].toString())];
				if (lastTested >= interval) {
					toTest.add(item);
				} else {
					notToTest.add(item);
				}
			}	
		}
		if (toTest.size() == 0) {
			System.out.println("No vocabulary to test today");
		} else {
			ArrayList<Object[]> wrongWords = new ArrayList<Object[]>();
			for (int i = 0; i < toTest.size(); i++) {
				Object[] testEntry = (Object[])toTest.get(i);
				Scanner scanner = new Scanner(System.in);
				System.out.println(LANG_FROM+": "+testEntry[M_FROM].toString());
				System.out.print(LANG_TO+": ");
				String answer = scanner.nextLine();
				if (answer.equals("!")) break;
				int wordClass = Integer.parseInt(testEntry[M_CLASS].toString());
				int testCount = Integer.parseInt(testEntry[M_TESTCOUNT].toString());
				int rightCount = Integer.parseInt(testEntry[M_SUCCCOUNT].toString());
				if (answer.equals(testEntry[M_TO])) {
					System.out.println("Correct answer!");
					rightCount++;
					if (wordClass < wordClasses.length - 1) wordClass++;
				} else {
					wrongWords.add(testEntry);
					System.out.println("Wrong answer! The right answer is " + testEntry[M_TO]);
					if (wordClass > 0) wordClass--; 
				}
				if(!"".equals(testEntry[M_COMMENT])) System.out.println("\"" + testEntry[M_COMMENT] + "\"");
				testCount++;
				Object[] testedEntry = new Object[]{testEntry[M_FROM],testEntry[M_TO],testEntry[M_COMMENT],wordClass,dateFormatter.format(new Date()),testCount, rightCount};
				tested.add(testedEntry);
			}
			for (Object[] object:wrongWords) {
				System.out.println("[!]\t"+LANG_FROM+":"+object[M_FROM]+"\t"+LANG_TO+":"+object[M_TO]);
			}
			vocabulary.clear();
			for (int i = 0; i < toTest.size(); i++) {
				Object[] newEntry;
				if (i < tested.size()) {
					newEntry = (Object[])tested.get(i);
				} else {
					newEntry = (Object[])toTest.get(i);
				}
				vocabulary.add(newEntry);
			}
			for (int i = 0; i < notToTest.size(); i++) {
				vocabulary.add((Object[])notToTest.get(i));
			}
			writeVocFile();
		}
	}
	
	private static void stats() {
		if (vocabulary.size() == 0) readVocFile();
		int[] ocurrence = new int[wordClasses.length];
		Arrays.fill(ocurrence,0);
		for (Object object:vocabulary) {
			int wordClass = Integer.parseInt(((Object[])object)[M_CLASS].toString());
			ocurrence[wordClass] += 1;
		}
		for (int i = 0; i<ocurrence.length; i++) {
			System.out.println(wordClasses[i]+" day" + (wordClasses[i] != 1 ? "s" : "") + "\t class has " + ocurrence[i] + " word" + (ocurrence[i] != 1 ? "s" : ""));
		}
	}
	
	private static void runTest() {
		ArrayList<String[]> usingVocabylary = minimalVocabulary();
		vocabulary.add(new Object[]{usingVocabylary.get(0)[0],usingVocabylary.get(0)[1],"",0,"NULL",0,0});
		for (int i = 0; i < wordClasses.length; i++) {
			System.out.println("Loop for the " + i + "word class");
			Date now = new Date();
			Calendar c = new GregorianCalendar();
			c.add(Calendar.DAY_OF_YEAR, -wordClasses[i]+1);
			String falseDate = dateFormatter.format(c.getTime());
			c.add(Calendar.DAY_OF_YEAR, -1);
			String trueDate = dateFormatter.format(c.getTime());
			vocabulary.add(new Object[]{usingVocabylary.get(2*i+1)[0],usingVocabylary.get(2*i+1)[1],"",i,falseDate,i,i});
			vocabulary.add(new Object[]{usingVocabylary.get(2*i+2)[0],usingVocabylary.get(2*i+2)[1],"",i,trueDate,i,i});
		}
		writeVocFile();
	}
	
	private static ArrayList<String[]> minimalVocabulary() {
		ArrayList<String[]> creatingVocabulary = new ArrayList<String[]>();
		creatingVocabulary.add(new String[]{"cachorro","perro"});
		creatingVocabulary.add(new String[]{"segunda-feira","lunes"});
		creatingVocabulary.add(new String[]{"ter�a-feira","martes"});
		creatingVocabulary.add(new String[]{"quarta-feira","mi�rcoles"});
		creatingVocabulary.add(new String[]{"quinta-feira","jueves"});
		creatingVocabulary.add(new String[]{"sexta-feira","viernes"});
		creatingVocabulary.add(new String[]{"s�bado","s�bado"});
		creatingVocabulary.add(new String[]{"domingo","domingo"});
		creatingVocabulary.add(new String[]{"vermelho","rojo"});
		creatingVocabulary.add(new String[]{"crian�a","ni�o"});
		creatingVocabulary.add(new String[]{"ver�o","verano"});
		creatingVocabulary.add(new String[]{"rua","calle"});
		creatingVocabulary.add(new String[]{"�nibus","autobus"});
		creatingVocabulary.add(new String[]{"gr�vida","embarazada"});
		creatingVocabulary.add(new String[]{"cinza","gris"});
		creatingVocabulary.add(new String[]{"frango","pollo"});
		creatingVocabulary.add(new String[]{"av�","abuela"});
		creatingVocabulary.add(new String[]{"filho","hijo"});
		creatingVocabulary.add(new String[]{"chica","garota"});
		creatingVocabulary.add(new String[]{"presunto","jam�n york"});
		creatingVocabulary.add(new String[]{"presunto cru","jam�n"});
		creatingVocabulary.add(new String[]{"carro","coche"});
		creatingVocabulary.add(new String[]{"alface","lechuga"});
		creatingVocabulary.add(new String[]{"cidade","ciudad"});
		creatingVocabulary.add(new String[]{"cidad�o","ciudadano"});
		creatingVocabulary.add(new String[]{"prefeitura","ayuntamiento"});
		creatingVocabulary.add(new String[]{"morango","fresa"});
		creatingVocabulary.add(new String[]{"banana","pl�tano"});
		creatingVocabulary.add(new String[]{"p�sego","melocot�n"});
		creatingVocabulary.add(new String[]{"laranja","naranja"});
		creatingVocabulary.add(new String[]{"abacaxi","pi�a"});
		creatingVocabulary.add(new String[]{"ovo","huevo"});
		creatingVocabulary.add(new String[]{"loja","tienda"});
		return creatingVocabulary;
	}
	
	private static boolean isWindows() {
		return SYS_OS.indexOf("win") >= 0 ? true : false;
	}
	
}
