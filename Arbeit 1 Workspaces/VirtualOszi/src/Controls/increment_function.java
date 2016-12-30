package Controls;

public class increment_function {


    //Funkion fkt_incrementButton_1
	
	/*
	 * Ist eine Funktion, die von einem lowlimit aus zu einem highlimit hochzählt.
	 * Dies geschiet immer mit der Zahlenfolge 2, 5, 10... und den passenenen Präfixen
	 * Wird der Incrementbutton gedrückt, so erfolgt die Feineinstellung, welche in Intervalle 
	 * untergliedert ist
	 * Diese Funktion Wird für die Button 
	 *  -> Horizontaler Skalenknop 
	 *  -> Vc_Skalenknopf 1-4 
	 *  benutzt 
	 */

    public static double fkt_incrementButton_1(double actwert, boolean toggle, double lowlim, double highlim, int actnotch)
    {	// Aktueller wert wird immer so zerlegt, das er sich im Intwervall von [1,10] bewegt
        int i = 0;
        double retval;
        while(actwert<1) {
            actwert=actwert*10;
            i++;
        }
        while(actwert>=10) {
            actwert = actwert / 10;
            i--;
        }
        // springt fkt funktioniert bei nichtgedrücktem Button
        if(!toggle) {
            if(actnotch>0) {//Runterzählen
                if(actwert >=5) {
                    actwert = 1;
                    i--;
                }	else if(actwert >=2){
                    actwert =5;
                }	else {
                    actwert = 2;
                }
            } else if (actnotch < 0){//Runterzählen
                if(actwert >5) {
                    actwert = 5;
                }	else if(actwert >2){
                    actwert =2;
                }	else if(actwert > 1){
                    actwert = 1;
                } else {
                    actwert = 5;
                    i++;
                }
            }
            retval = (actwert/Math.pow(10, i));//Zusammenfügen von Aktueller wert und 10er-Potenz = Rückgabewert
            //Intervalle bei gedrücktem Knopf
        } else {// Intervallweise in genauen Zwischenschritten hochzählen
            if(actnotch>0){
                if((actwert/Math.pow(10, i))>=2/Math.pow(10, 9) && (actwert/Math.pow(10, i))<20/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(0.5/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=20/Math.pow(10, 9) && (actwert/Math.pow(10, i))<50/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(1/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=50/Math.pow(10, 9) && (actwert/Math.pow(10, i))<100/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(2/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=100/Math.pow(10, 9) && (actwert/Math.pow(10, i))<200/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(4/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=200/Math.pow(10, 9) && (actwert/Math.pow(10, i))<500/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(10/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=500/Math.pow(10, 9) && (actwert/Math.pow(10, i))<1000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(20/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=1000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<2000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(40/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=2000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<5000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(100/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=5000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<10000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(200/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=10000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<20000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(400/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=20000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<50000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(1000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=50000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<100000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(2000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=100000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<200000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))+(4000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>=200/Math.pow(10, 6) && (actwert/Math.pow(10, i))<500/Math.pow(10, 6)){
                    actwert=(actwert/Math.pow(10,i))+(10/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=500/Math.pow(10, 6) && (actwert/Math.pow(10, i))<1000/Math.pow(10, 6)){
                    actwert=(actwert/Math.pow(10,i))+(20/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=1/Math.pow(10, 3) && (actwert/Math.pow(10, i))<2/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(40/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=2/Math.pow(10, 3) && (actwert/Math.pow(10, i))<5/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(100/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=5/Math.pow(10, 3) && (actwert/Math.pow(10, i))<10/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(200/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=10/Math.pow(10, 3) && (actwert/Math.pow(10, i))<20/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(400/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>=20/Math.pow(10, 3) && (actwert/Math.pow(10, i))<50/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(1/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=50/Math.pow(10, 3) && (actwert/Math.pow(10, i))<100/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(2/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=100/Math.pow(10, 3) && (actwert/Math.pow(10, i))<200/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(4/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=200/Math.pow(10, 3) && (actwert/Math.pow(10, i))<500/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(10/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=500/Math.pow(10, 3) && (actwert/Math.pow(10, i))<1000/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))+(20/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=1 && (actwert/Math.pow(10, i))<2){
                    actwert=(actwert/Math.pow(10,i))+(40/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=2 && (actwert/Math.pow(10, i))<5){
                    actwert=(actwert/Math.pow(10,i))+(100/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=5 && (actwert/Math.pow(10, i))<10){
                    actwert=(actwert/Math.pow(10,i))+(200/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=10 && (actwert/Math.pow(10, i))<20){
                    actwert=(actwert/Math.pow(10,i))+(400/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>=20 && (actwert/Math.pow(10, i))<50){
                    actwert=(actwert/Math.pow(10,i))+1;
                }

            }
            else if(actnotch < 0){// Intervallweise in genauen Zwischenschritten runterzählen
                if((actwert/Math.pow(10, i))>2/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=20/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(0.5/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>20/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=50/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(1/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>50/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=100/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(2/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>100/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=200/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(4/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>200/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=500/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(10/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>500/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=1000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(20/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>1000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=2000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(40/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>2000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=5000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(100/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>5000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=10000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(200/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>10000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=20000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(400/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>20000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=50000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(1000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>50000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=100000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(2000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>100000/Math.pow(10, 9) && (actwert/Math.pow(10, i))<=200000/Math.pow(10, 9)){
                    actwert=(actwert/Math.pow(10,i))-(4000/Math.pow(10, 9));
                }else if((actwert/Math.pow(10, i))>200/Math.pow(10, 6) && (actwert/Math.pow(10, i))<=500/Math.pow(10, 6)){
                    actwert=(actwert/Math.pow(10,i))-(10/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>500/Math.pow(10, 6) && (actwert/Math.pow(10, i))<=1000/Math.pow(10, 6)){
                    actwert=(actwert/Math.pow(10,i))-(20/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>1/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=2/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(40/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>2/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=5/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(100/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>5/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=10/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(200/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>10/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=20/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(400/Math.pow(10, 6));
                }else if((actwert/Math.pow(10, i))>20/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=50/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(1/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>50/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=100/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(2/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>100/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=200/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(4/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>200/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=500/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(10/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>500/Math.pow(10, 3) && (actwert/Math.pow(10, i))<=1000/Math.pow(10, 3)){
                    actwert=(actwert/Math.pow(10,i))-(20/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>1 && (actwert/Math.pow(10, i))<=2){
                    actwert=(actwert/Math.pow(10,i))-(40/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>2 && (actwert/Math.pow(10, i))<=5){
                    actwert=(actwert/Math.pow(10,i))-(100/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>5 && (actwert/Math.pow(10, i))<=10){
                    actwert=(actwert/Math.pow(10,i))-(200/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>10 && (actwert/Math.pow(10, i))<=20){
                    actwert=(actwert/Math.pow(10,i))-(400/Math.pow(10, 3));
                }else if((actwert/Math.pow(10, i))>20 && (actwert/Math.pow(10, i))<=50){
                    actwert=(actwert/Math.pow(10,i))-1;
                }
            }
            
          //Aenderung von M. Kessel 23.02.2016
            //actwert wird auf 6 nachkommastellen gerundet
            actwert = Math.round(actwert*Math.pow(10,6))/Math.pow(10,6);
            retval=actwert;	//Rückgabewert
            //	retval = (2/Math.pow(10, 9));
        }

        if(retval < lowlim) {
            retval = lowlim;
        }
        if(retval > highlim) {
            retval = highlim;
        }
        return retval;

        // 	if(actnotch>0)
        //	while()
        //
    }




    // fkt_incrementButton_2
    
    /*
     * Dies ist eine Funktion die, die bei jedem Tick entweder 
     * den Aktuellen Buttonwert um eins inkrementiert oder um 
     * eins dekrementiert
     * Wird der Icrementbutton gedrückt, so wird der Aktuelle Wert auf 0 
     * zurück gesetzt
     * 
     * Diee Funktion wird benutz bei den Buttons:
     *  ->Horizontaler Positionsknopf
     */

    public static int fkt_incrementButton_2(int actwert, boolean toggle, int actnotch)
    {
        if(!toggle) {
            if(actnotch>0) {
                actwert += 1;
            }
            else if (actnotch < 0){
                actwert -=1;
            }

        }else {
            actwert=0;
        }
        return actwert;
    }


    // fkt_incrementButton_3
    
    /*
     * Dies ist eine Funktion die, die bei jedem Tick entweder 
     * den aktuellen Buttonwert um eins inkrementiert oder um 
     * eins dekrementiert
     * Wird der Icrementbutton gedrückt, so wird der aktuelle Wert 
     * auf 50% des aktuellen Werts geändert
     * 
     * Diee Funktion wird benutz bei den Buttons:
     *  ->Horizontaler Positionsknopf
     */

    public static double fkt_incrementButton_3(double actwert, boolean toggle, int actnotch)
    {
        if(!toggle) {
            if(actnotch>0) {
                actwert += 1;
            }
            else if (actnotch < 0){
                actwert -=1;
            }

        }else {
            actwert /= 2;
        }
        return actwert;
    }

// fkt_incrementButton_4
    
    /*
     * Dies ist eine Funktion die, die bei jedem Tick 
     * den wert von einer Anderen Funktion abgleicht und 
     * je nach dem in Verschiedenen Inervallen unterschieldich 
     * hoch oder runter zählt
     * 
     * Wird der Incrementbutton gedrückt, so wird der actwert auf 0 gresetz. 
     * Diee Funktion wird benutz bei den Buttons:
     *  ->Horizontaler Positionsknopf
     */

    public static double fkt_incrementButton_3(double actwert, boolean toggle, int actnotch, double vergleich)
    {
        if(!toggle) {
            if(actnotch>0) {
                if((vergleich >= 1/Math.pow(10,3)) && (vergleich < 2/Math.pow(10,3))){
                    actwert += 25/Math.pow(10, 6);
                } else if((vergleich >= 2/Math.pow(10, 3)) && (vergleich < 5/Math.pow(10,3))){
                    actwert += 25/Math.pow(10, 6);
                } else if((vergleich >= 5/Math.pow(10, 3)) && (vergleich < 10/Math.pow(10,3))){
                    actwert += 50/Math.pow(10, 6);
                }else if((vergleich >= 10/Math.pow(10, 3)) && (vergleich < 20/Math.pow(10,3))){
                    actwert += 125/Math.pow(10, 6);
                }else if((vergleich >= 20/Math.pow(10, 3)) && (vergleich < 50/Math.pow(10,3))){
                    actwert += 250/Math.pow(10, 6);
                }else if((vergleich >= 50/Math.pow(10, 3)) && (vergleich < 100/Math.pow(10,3))){
                    actwert += 625/Math.pow(10, 6);
                }else if((vergleich >= 100/Math.pow(10, 3)) && (vergleich < 200/Math.pow(10,3))){
                    actwert += 1250/Math.pow(10, 6);
                }else if((vergleich >= 200/Math.pow(10, 3)) && (vergleich < 500/Math.pow(10,3))){
                    actwert += 2500/Math.pow(10, 6);
                }else if((vergleich >= 500/Math.pow(10, 3)) && (vergleich < 1000/Math.pow(10,3))){
                    actwert += 6250/Math.pow(10, 6);
                }else if((vergleich >= 1) && (vergleich < 2)){
                    actwert += 12500/Math.pow(10, 6);
                }else if((vergleich >= 2) && (vergleich <= 5)){
                    actwert += 25000/Math.pow(10, 6);
                }
            }
            else if (actnotch < 0){
                if((vergleich >= 1/Math.pow(10,3)) && (vergleich < 2/Math.pow(10,3))){
                    actwert -= 25/Math.pow(10, 6);
                } else if((vergleich >= 2/Math.pow(10, 3)) && (vergleich < 5/Math.pow(10,3))){
                    actwert -= 25/Math.pow(10, 6);
                } else if((vergleich >= 5/Math.pow(10, 3)) && (vergleich < 10/Math.pow(10,3))){
                    actwert -= 50/Math.pow(10, 6);
                }else if((vergleich >= 10/Math.pow(10, 3)) && (vergleich < 20/Math.pow(10,3))){
                    actwert -= 125/Math.pow(10, 6);
                }else if((vergleich >= 20/Math.pow(10, 3)) && (vergleich < 50/Math.pow(10,3))){
                    actwert -= 250/Math.pow(10, 6);
                }else if((vergleich >= 50/Math.pow(10, 3)) && (vergleich < 100/Math.pow(10,3))){
                    actwert -= 625/Math.pow(10, 6);
                }else if((vergleich >= 100/Math.pow(10, 3)) && (vergleich < 200/Math.pow(10,3))){
                    actwert -= 1250/Math.pow(10, 6);
                }else if((vergleich >= 200/Math.pow(10, 3)) && (vergleich < 500/Math.pow(10,3))){
                    actwert -= 2500/Math.pow(10, 6);
                }else if((vergleich >= 500/Math.pow(10, 3)) && (vergleich < 1000/Math.pow(10,3))){
                    actwert -= 6250/Math.pow(10, 6);
                }else if((vergleich >= 1) && (vergleich < 2)){
                    actwert -= 12500/Math.pow(10, 6);
                }else if((vergleich >= 2) && (vergleich <= 5)){
                    actwert -= 25000/Math.pow(10, 6);
                }
            }

        }else {
            actwert = 0;
        }

        //Aenderung von M. Kessel 23.02.2016
        //actwert wird auf 6 nachkommastellen gerundet
        actwert = Math.round(actwert*Math.pow(10,6))/Math.pow(10,6);
        return actwert;
    }
}

