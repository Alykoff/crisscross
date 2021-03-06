/**
 * WordArea
 *
 * @version 1.00 
 * @author Michael Kalinin, Alykoff Gali
 */
 
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*; 
import java.awt.font.*;
import javax.swing.*; 

@SuppressWarnings("serial")
public class WordArea extends JComponent {
		private ArrayWord<Word> mainWordArea = null;
		private ArrayList<ArrayWord<Word>> allWordArea;
		private Font font;
		private FontRenderContext context;
		private int width;
		private int height;
		private List<String> rawWords;
		private int numberOfWord;
		private int sizeWordArea = 1000;
		private double density = 0;//WTF
		private int k = 0;//WTF??? count variable?
		private double alpha = 0;
		
		//TODO
    public WordArea () {  
        try {
            readWords();            
            allWordArea = new ArrayList<ArrayWord<Word>>();
            alpha = (double) 1 / Math.pow( 1.4, numberOfWord );
            font = new Font ( "SansSerif", Font.PLAIN, 22 );
            String first = rawWords.remove(0);
            Word firstWord = new Word ( first, Orientation.HORIZ, 0, 0 );
            ArrayWord<Word> firstWordArea = new ArrayWord<Word>();
            firstWordArea.add( firstWord );
            
            wordsBackTracking( firstWordArea, rawWords );        
        
            Iterator<ArrayWord<Word>> iter = allWordArea.iterator();        
            while ( iter.hasNext() ) {
                assignCoordinate( iter.next() );
            }
            width = ( mainWordArea.width()+3 )*CharCell.CELL_SIZE;
            height = ( mainWordArea.height()+3 )*CharCell.CELL_SIZE;
            setSize( width, height );
        }
        catch ( IOException e ) {
            System.out.println( "Failed to read file." );
        }                
    }
    /**
     * Переопределяем paintComponent
     */
    @Override
  	public void paintComponent(Graphics graphics) {
  		Graphics2D graphics2D = (Graphics2D) graphics;
  		graphics2D.setRenderingHint(
  				RenderingHints.KEY_ANTIALIASING,
  				RenderingHints.VALUE_ANTIALIAS_ON
  		);
  		this.context = graphics2D.getFontRenderContext();
  		graphics2D.setFont(this.font);
  		this.showWords(graphics2D);
  	}  
    /**
     * Выводит готовую схему в графическом режиме.
     */    
    private void showWords ( Graphics2D g2D ) {
    	for (Word word : mainWordArea) {
    		word.showWord(g2D, font, context);
    	}
    }
    /**
     *  Перебор всех слов с возвратом (Backtracking) 
     *  Составляет схему крисс-кросс
     *  TODO
     */    
    private void wordsBackTracking ( ArrayWord<Word> wordArea, List<String> words ) {
                
        if ( accept( wordArea ) ) { 
            ArrayWord<Word> tempWordArea = new ArrayWord<Word>( wordArea );
            copyArrayWord( tempWordArea, wordArea );
            allWordArea.add( tempWordArea ); 
            mainWordArea = tempWordArea;
            return;
        }
        
        if ( reject( wordArea, words ) ) return;    
        
        for ( int i = 0 ; i < words.size() ; i++ ) {
            
            List<String> tempWords = new LinkedList<String>( words );
            String newWord = tempWords.get(i);
            tempWords.remove(i);            
            
            addNewWord( wordArea, tempWords, newWord );                        
        }                        
    }    
    /**
     * Добавляет новое слово, если это возможно
     * TODO
     */        
    private void addNewWord ( ArrayWord<Word> wordArea, List<String> words, String newWord ) {        
        Word existentWord;            
        for ( int k = 0 ; k < wordArea.size() ; k++ ) {
            existentWord = wordArea.get(k);
            ///сравниваем символы в новом и уже занесенном словах
            for ( int i = 0 ; i < existentWord.length() ; i++ ) {
                for ( int j = 0 ; j < newWord.length() ; j++ ) {
                    if ( existentWord.get(i).value() == newWord.charAt(j) ) {                                    
                        int newOrient = invert( existentWord.orientation() );
                        int newWordCoord = existentWord.get(i).coord();
                        int initialVariableCoord = existentWord.coord() - j;
                        Word word = new Word ( newWord, newOrient, newWordCoord, initialVariableCoord );
                        ///если слово "проходит" проверку - добавляем его
                        int interCount = wordArea.intersectCount();
                        if ( check ( wordArea, word, existentWord.coord() ) ) {                    
                            wordArea.add ( word );
                            ///сохраняем смещение относительно (0,0) сохранив предыдущие
                            int minX = wordArea.minX();
                            int minY = wordArea.minY();                            
                            if ( existentWord.orientation() == Orientation.HORIZ ) wordArea.setMinY( initialVariableCoord );                        
                            else wordArea.setMinX( initialVariableCoord );    
                            ///запускаем косвенную рекурсию			                        
                            wordsBackTracking ( wordArea, words );    
                            ///убираем последнее добавленное слово
                            wordArea.remove( wordArea.size()-1 );
                            wordArea.reset();
                            wordArea.setMinX( minX );    
                            wordArea.setMinY( minY );
                            wordArea.setInterCount( interCount );                                                
                        }
                    }            
                }            
            } 
        }        
    } 
    /**
     *  Отклоняет потенциально не оптимальные частичные решения
     *  TODO
     */    
    private boolean reject ( ArrayWord<Word> wordArea, List<String> words ) {
    	 ///площадь текущей схемы не больше площади полного решения		       
      int currentSize = calcSizeWordArea(wordArea);
      if (currentSize >= sizeWordArea) {
      	return true;
      }
      ///плотность текущей схемы не меньше плотности полного решения	   
      double currentDensity = (double) wordArea.intersectCount() / currentSize;                
      if ( currentDensity+alpha < density ) {
      	return true;
      }        
      ///средняя длина слов в схеме меньше чем средняя длина оставшихся слов
      double averageLengthWA = (double) sumWordLength(wordArea) / wordArea.size();
      int sumLengthWL = 0;
      Iterator<String> iter = words.iterator();        
      while ( iter.hasNext() ) 
          sumLengthWL += iter.next().length();
      double averageLengthWL = (double) sumLengthWL / words.size();
      if ( averageLengthWA >= averageLengthWL ) return true;
      
      return false;
    }
    /**
     *  Проверяет является ли схема решением
     */    
    private boolean accept ( ArrayWord<Word> wordArea ) {
    	if (wordArea.size() != this.numberOfWord) {
    		return false;
    	}
    	int currentSize = this.calcSizeWordArea(wordArea);
    	int intersectCount = wordArea.intersectCount();
    	double currentDensity = (double) intersectCount / currentSize;

    	if (currentDensity <= density) {
    		return false;
    	}
    	this.sizeWordArea = currentSize;
    	this.density = currentDensity;                
    	return true;
    }
    /**
     * Проверяет возможность добавления нового слова в найденное место	 
     * orient - ориентация добавляемого слова + находит число пересечений со словом
     * WTF!!!!!
     * TODO
     */    
    private boolean check ( ArrayWord<Word> wordArea, Word newWord, int intersect ) {
    
        Iterator<Word> wordAreaIter = wordArea.iterator();    
        Word word;
        
        int intersectCount = wordArea.intersectCount();
        int orient = newWord.orientation();
        int newWordCoord = newWord.coord();        
        int newFirst = newWord.first();
        int newLast = newWord.last();            
        
        while ( wordAreaIter.hasNext() ) {
            word = wordAreaIter.next();
            int existFirst = word.first();
            int existLast = word.last();
            ///проверяем все слова в этом же положении что и добавляемое
            if ( word.orientation () == orient ) {                
                if ( word.coord() == newWordCoord - 1 || word.coord() == newWordCoord + 1 ) {
                    if ( !(    (newFirst == existLast) && (newFirst == intersect) ) && 
                         !( (newLast == existFirst) && (newLast == intersect) ) )
                        if ( intersect( newFirst, newLast, existFirst, existLast) )                    
                            return false;                            
                }
                else 
                if ( word.coord() == newWordCoord ) {
                    if ( intersect( newFirst-1, newLast+1, existFirst, existLast) )                    
                         return false;
                }                                
            }
            ///и в противоположном
            else {
                ///слова лежащие непосредственно в координах добавляемого слова
                if ( range ( newFirst, newLast, word.coord() ) ) {
                    for ( int i = 0 ; i < word.length() ; i++ ) {
                        for ( int j = 0 ; j < newWord.length() ; j++ ) {
                            if ( word.get(i).coord() == newWordCoord && newWord.get(j).coord() == word.coord() ) 
                                if ( word.get(i).value() != newWord.get(j).value() ) return false;
                                else intersectCount++;
                        }
                    }
                    if ( ( existFirst == newWordCoord + 1 ) || ( existLast == newWordCoord - 1 ) ) 
                        return false;
                }
                ///слова лежащие по бокам от добавляемого слова
                if ( word.coord() == newFirst - 1 || word.coord() == newLast + 1 ) {
                    if ( range ( existFirst, existLast, newWordCoord ) ) 
                        return false;                                        
                }
            }
        }    
        if ( wordArea.intersectCount() == intersectCount ) wordArea.setInterCount( ++intersectCount );
        else wordArea.setInterCount( intersectCount ); 
        return true;
    }    
    /**
     * Проверяет пересекаются ли 2 отрезка [a,b] и [c,d]	     
     */
    private boolean intersect ( int a, int b, int c, int d ) {
        return 
        		range(a, b, c) ||
        		range(a, b, d) ||
        		range(c, d, a) ||
        		range(c, d, b);
    }
    /**
     * Проверяет принадлежит ли х отрезку [a,b]	
     */
    private boolean range ( int a, int b, int x ) {
        return x >= a && x <= b;
    }    
    /**
     * Рассчитывает размер(площадь) схемы 
     * TODO  
     */
    private int calcSizeWordArea ( ArrayWord<Word> wordArea ) {
        if ( wordArea.size() == 0 ) return 0;
        Iterator<Word> wordAreaIter = wordArea.iterator();
        Word word;
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        while ( wordAreaIter.hasNext() ) {
            word = wordAreaIter.next();    
            int wordCoord = word.coord();
            int first = word.first();
            int last = word.last();
            if ( word.orientation () == Orientation.HORIZ ) {
                if ( wordCoord < minY ) minY = wordCoord;
                if ( wordCoord > maxY ) maxY = wordCoord;
                if ( first < minX ) minX = first;
                if ( last > maxX ) maxX = last;
            }
            else {
                if ( wordCoord < minX ) minX = wordCoord;
                if ( wordCoord > maxX ) maxX = wordCoord;
                if ( first < minY ) minY = first;
                if ( last > maxY ) maxY = last;
            }
        }
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        wordArea.setWidth( width );
        wordArea.setHeight( height );
        return ( width * height );
    }
    /**
     * Рассчитывает суммарную длину слов схемы   
     */
    private int sumWordLength(ArrayWord<Word> wordArea) {
        int result = 0;
        for (Word word : wordArea) {
        	result += word.length();
        }
        return result;
    }
    /**
     * Устанавливает допустимые значения координат
     */    
    private void assignCoordinate ( ArrayWord<Word> wordArea ) {
    	for (Word word : wordArea) {
    		word.increaseCoordinate(
    				wordArea.minX() - 1, 
    				wordArea.minY() - 1
    		);
    	}
    	wordArea.reset();
    }    
    /**
     * Инвертирует положение	
     * TODO HORIZ and VERTIC
     */    
    private int invert(int orient) {
        if (orient == Orientation.HORIZ) {
        	return Orientation.VERTIC;
        }
        return Orientation.HORIZ;
    }
    /**
     * Копирует массив слов 
     */
    private void copyArrayWord(
    		ArrayWord<Word> newArray,
    		ArrayWord<Word> initialArray) {
    			for (Word word : initialArray) {
    				newArray.add(word.clone());
    			}
    }
    /**
     * Читает список слов из файла
     */
    private void readWords() throws IOException {
        this.rawWords = new LinkedList<String>();
        FileReader file = new FileReader("words.txt");
        Scanner in = new Scanner(file);
        while (in.hasNext()) {
            String line = in.next();
            this.rawWords.add(line);
        }
        this.numberOfWord = this.rawWords.size();
    }
    /**
     * Переключение между найденными схемами
     * TODO
     */    
    public void nextArea() {
        if ( k < allWordArea.size() ) {
            mainWordArea = allWordArea.get( k++ );
        }
        else {
            k = 0;
            mainWordArea = allWordArea.get( k++ );
        }
        width = ( mainWordArea.width()+3 )*CharCell.CELL_SIZE;
        height = ( mainWordArea.height()+3 )*CharCell.CELL_SIZE;
        setSize( width, height );
        repaint();            
    }  
    
    public int getwidth() {
    	return width;
    }
    
    public int getheight() {
    	return height;
    }
}

