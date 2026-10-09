# Chef_Expense_Tracking_System_Desktop

Java 17+ / Maven / Swing alapú asztali alkalmazás séfek költségeinek nyilvántartására. Nem igényel külső függőséget (csak a JDK-t, tesztekhez JUnit-ot). A felület magyar nyelvű, kinézete a `resources/chef_expenses_patterns` képeket követi.

## Követelmények

- JDK 17 vagy újabb (a fejlesztés JDK 21-gyel történt).
- Maven 3.8+ (az IntelliJ IDEA beépített Mavenjével is működik).

## Funkciók

- Indításkor automatikusan betölti a projektbe ágyazott `src/main/resources/chef_koltsegek_2025.csv` fájlt (kifejezetten UTF-8, `;` elválasztó, az első sor fejléc, nem adat).
- Táblázatban jeleníti meg az összes költséget (Id, ChefName, Datum, Kategoria, Osszeg, Megjegyzes).
- Új költség felvétele űrlappal: séf neve, dátum, kategória (Travel, Ingredients, Accommodation, Equipment, Other), összeg (EUR), megjegyzés (opcionális) és a **Hozzáadás** gomb.
- Hozzáadáskor: kötelező mezők, dátum (2025-ös) és összeg (pozitív, legfeljebb két tizedesjegy; vessző vagy pont is elfogadott) ellenőrzése; az azonosítót az alkalmazás generálja (legnagyobb meglévő id + 1); a táblázat azonnal frissül; a teljes adatállomány újraíródik a CSV-be; az űrlap kiürül.
- Hiba esetén (érvénytelen adat vagy sikertelen mentés) magyar hibaüzenet jelenik meg. A CSV mentése előbb történik, mint a memóriabeli lista bővítése, ezért sikertelen mentésnél a lista, a tábla és a fájl nem kerül ellentmondásba.

## CSV formátum és speciális karakterek

Fejléc: `id;chefname;datum;kategoria;osszeg;megjegyzes` (UTF-8, BOM nélkül). A dátum `yyyy-MM-dd`, az összeg tizedespontos szám (pl. `1000.5`).

A fájlszerkezet védelme érdekében a séf neve és a megjegyzés **nem tartalmazhat pontosvesszőt** (az alkalmazás hibaüzenettel elutasítja), a megjegyzésbe írt sortörések szóközzé alakulnak, így egy rekord mindig egy sor. A mentés ideiglenes fájlon keresztül történik, ezért hiba esetén az eredeti CSV nem csonkul.

## Mappaszerkezet

    DualisKepzes_Feladatok/
      resources/                           a forrásfájlok közös másolata és a referenciaképek
      Chef_Expense_Tracking_System_Desktop/src/main/resources/
        chef_koltsegek_2025.csv            ennek a projektnek az adatfájlja (beágyazva)
        chef_expenses_patterns/            képernyőképek a kinézethez
      Chef_Statistic_System_Console/       1. feladat
      Chef_Expense_Tracking_System_Desktop/  ez a projekt
        pom.xml
        src/main/java/hu/chefexpense/
          Main.java                        indítás
          model/Expense.java               költség modell (record)
          data/ExpenseCsv.java             egy CSV sor ↔ Expense
          data/ExpenseRepository.java      betöltés, ellenőrzés, id-generálás, mentés
          gui/MainFrame.java               ablak (tábla + űrlap)
          gui/ExpenseTableModel.java       táblamodell
        src/test/java/...                  egységtesztek

## A közös CSV megkeresése

A program a projekt saját `src/main/resources/chef_koltsegek_2025.csv` fájlját használja: a lefordított osztályok helyétől (`target/classes` vagy a jar) és a munkakönyvtártól indulva felfelé keresi. Így az IntelliJ-ből, VS Code-ból, Mavenből, `java -jar`-ral, a projektmappából és a workspace gyökeréből indítva is megtalálja. A forrásfájlt írja (nem a `target/classes` másolatot), így a hozzáadott költségek újrafordítás után is megmaradnak. Eltérő helyen lévő fájl az első programargumentummal adható meg.

## Importálás IntelliJ IDEA-ban

1. File > Open, válassza ki a `Chef_Expense_Tracking_System_Desktop` mappát (amelyben a `pom.xml` van), majd Trust Project. A Maven automatikusan importál.
2. File > Project Structure > Project SDK: JDK 17 vagy újabb.
3. Futtatás: nyissa meg a `src/main/java/hu/chefexpense/Main.java` fájlt, és kattintson a `main` melletti zöld nyílra > Run 'Main.main()'.
4. Külön beállítás nem szükséges. Ha a CSV mégsem található, a Run > Edit Configurations > Working directory legyen a projektmappa vagy a workspace gyökere.

## Fordítás és futtatás

IntelliJ-ban: Maven panel > Lifecycle > `package` (fordítás és tesztek), vagy Build > Build Project.

Parancssorból:

    cd Chef_Expense_Tracking_System_Desktop
    mvn test                      # egységtesztek
    mvn package                   # target/chef-expense-tracking-system-desktop-1.0.0.jar
    mvn compile exec:java         # futtatás Mavenből
    java -jar target/chef-expense-tracking-system-desktop-1.0.0.jar    # futtatás a jarból

## Megjegyzés a közös CSV-kről

A forrásfájl (`FORRAS_26_05_22/chef_koltsegek_2025.csv`) egy nem létező dátumot tartalmazott (95. sor: 2025-02-29); a beágyazott másolatban ez 2025-02-28-ra javítva.
