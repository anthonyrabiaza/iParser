# iParser

I wanted to share a solution I recently developed to have allow parsing of Flat File (CSV) and application of different operations:
- reading/skipping some lines
- transposing (row-oriented to column-oriented)
- getting list of unique values from column
- intelligent computation of indicators

![Alt text](resources/iParser.png?raw=true "iParser")

<aside class="warning">
!!! All the CSV file are comma separated, to ease reading the example here are using tab separated. !!!
</aside>

## Getting Started

Please download the [latest library](iparser-1.0.jar?raw=true), create a custom library (Scripting):

![Alt text](resources/Custom_Library.png?raw=true "iParser")

Then deploy the library in your runtime.

## Scenarios

All the scenario take the assumptions that the current Document contains Data in Flat File format.

### Reading or skipping some lines

You can use iparse.readFixedLines(<inputStream>,<first line index>,<last line index>)

For instance:

```
import java.util.Properties;
import java.io.InputStream;
import com.boomi.proserv.iparser.*;
import java.io.ByteArrayInputStream;

IParser iparser = new IParser();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    
    is = iparser.transpose(is);

    dataContext.storeStream(is, props);
}
```

### Transposing (row-oriented to column-oriented) Flat File

You can use iparse.transpose(<inputStream>)

For instance:

```
import java.util.Properties;
import java.io.InputStream;
import com.boomi.proserv.iparser.*;
import java.io.ByteArrayInputStream;

IParser iparser = new IParser();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    
    is = iparser.transpose(is);

    dataContext.storeStream(is, props);
}
```

### Getting list of unique values from column

```
Set<String> listOfPossibleValues = iparser.getPossiblesValues(is, "A");

```

### Intelligent computation of indicators

#### Processing of the Document using a Configuration File

<aside class="warning">
!!! All the CSV file are comma separated, to ease reading the example here are using tab separated. !!!
</aside>


Sample of Data to processed

```
A:Gender	B:AgeCategory		C:Departement	D:FTETotal	E:TraineeTotal
MAN		age<30 Yrs		iPaaS		10			15
MAN		age>30 Yrs		iPaaS		70			2
WOMAN		age<30 Yrs		iPaaS		15			7
WOMAN		age>30 Yrs		iPaaS		70			0
MAN		age<30 Yrs		ETL		5			1
MAN		age>30 Yrs		ETL		35			0
WOMAN		age<30 Yrs		ETL		7			0
WOMAN		age>30 Yrs		ETL		25			0
```

Configuration file

```
Type		Name		Label						Filter			Operation	OperationDetails
Indicator	ID1		Number of men with age below 30 years old	A=MAN,B=.*<30 Yrs	sum
Indicator	ID2		Number of women with age below 30 years old	A=WOMAN,B=.*<30 Yrs	sum
Indicator	ID3		Total number with age below 30 years old				compute		ID1+ID2
Indicator	ID4		Total number of trainees						sum(D)		

```
- Type: Indicator or Virtual (value is used for display only - not used for processing)
- Name: Name of the indicator
- Label: Description of the descriptor 
- Filter: Regular expression (one or several separated with comma)
	You have to put the column letter and the regular expression to apply (column A equals to MAN will be A=MAN)
- Operation: sum, div, avg, min, max, count or compute (for Virtual indicator)
	If no parameter is provided (for instance sum) the operation will be applied to all the data column, otherwise a column need to be provided (for instance sum(D))
- OperationDetails: only for Virtual indicator, when an calculation on severals Indicator (or Virtual Indicator) need to be applied

```
is = iparser.processFile(<configurationFile location>, inputstream, <dataColumnStart>, <dataColumnEnd>);
```

```
import java.util.Properties;
import java.io.InputStream;
import com.boomi.proserv.iparser.*;
import java.io.ByteArrayInputStream;

IParser iparser = new IParser();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    is = iparser.processFile(
        "work/iParser_Configuration_Headcount_Indicators.csv"
        , is, "D", "E");

    dataContext.storeStream(is, props);
}
```

Output:

```
"Indicator","ID1",31.0
"Indicator","ID2",29.0
"Virtual","ID3",60.0
"Indicator","ID4",237.0
```


#### GroupBy

You can processFileGroupBy and provide an additional parameter with is the GroupByColumn, here **"C"**

```
    is = iparser.processFileGroupBy(
        "work/iParser_Configuration_Headcount_Indicators.csv"
        , is, "D", "E",
        "C");
```

Output:

```
"Indicator","ID1","C=iPaaS",25.0
"Indicator","ID2","C=iPaaS",22.0
"Virtual","ID3","C=iPaaS",47.0
"Indicator","ID4","C=iPaaS",165.0
"Indicator","ID1","C=ETL",6.0
"Indicator","ID2","C=ETL",7.0
"Virtual","ID3","C=ETL",13.0
"Indicator","ID4","C=ETL",72.0
```