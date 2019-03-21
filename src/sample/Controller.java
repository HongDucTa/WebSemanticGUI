package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    ComboBox comboBoxCriteria;

    @FXML
    RadioButton radioInclude;

    @FXML
    RadioButton radioExclude;

    @FXML
    TableView resultTable;

    @FXML
    TableColumn titleTable;

    @FXML
    TableColumn yearTable;
    @FXML
    TableColumn countryTable;
    @FXML
    TableColumn genreTable;
    @FXML
    TableColumn actorsTable;
    @FXML
    TableColumn directorTable;

    @FXML
    TextField criteriaValue;

    @FXML
    TableView criteriaTable;


    @Override
    public void initialize(URL location, ResourceBundle resources){
        ObservableList<String> optionsCriteria = FXCollections.observableArrayList(
                "Actor",
                "Director",
                "Genre"
        );
        comboBoxCriteria.setItems(optionsCriteria);
        comboBoxCriteria.getSelectionModel().select(0);
    }

    public void enableInclude()
    {
        radioInclude.setSelected(true);
        radioExclude.setSelected(false);
    }

    public void enableExclude()
    {
        radioExclude.setSelected(true);
        radioInclude.setSelected(false);
    }

    public void addFilter()
    {
        String value = criteriaValue.getText();
        Boolean includeCriteria = radioInclude.isSelected();
        String criteria = comboBoxCriteria.getSelectionModel().getSelectedItem().toString();

        Condition conditionRow = new Condition(criteria,value,includeCriteria.toString());
        criteriaTable.getItems().add(conditionRow);
    }

    public String generateQueryFromTable()
    {
        ObservableList<Condition> conditionsList = criteriaTable.getItems();
        String condition = "";
        String criteria;
        String criteriaValue;
        String includeCriteria;
        String row;
        String boundEntity;
        for (int i = 0;i < conditionsList.size();i++)
        {
            row = "";
            criteria = conditionsList.get(i).getCriteria();
            criteriaValue = conditionsList.get(i).getValue();
            includeCriteria = conditionsList.get(i).getIncludeOrExclude();
            switch (criteria)
            {
                case "Actor":
                    row = "?movie :hasActor ?actor ." +
                            "?actor :hasName \""+ criteriaValue + "\"";
                    break;
                case "Director":
                    row = "?movie :hasDirector ?director ." +
                            "?director :hasName \"" + criteriaValue + "\"";
                    break;
                case "Genre":
                    row = row + ":hasGenre :" + criteriaValue;
                default:
                    break;
            }
            System.out.println("->" + row);

            if (includeCriteria.equals("false"))
            {
                row = "filter not exists { " + row + " }";
            }

            condition = condition + row + " .\n";
        }
        return condition;
    }

    public void clearFilter()
    {
        criteriaTable.getItems().remove(criteriaTable.getSelectionModel().getFocusedIndex());
    }

    public void clearAllFilters()
    {
        criteriaTable.getItems().remove(0,criteriaTable.getItems().size());
    }

    public void clearResults()
    {
        resultTable.getItems().remove(0,resultTable.getItems().size());
    }

    public void executeQuery()
    {
        clearResults();
        String conditions = generateQueryFromTable();

        Model model = ModelFactory.createDefaultModel();
        String filePath = new File("").getAbsolutePath();
        // read the TTL file
        model.read(filePath + "\\src\\sample\\final.ttl", null);

        String queryString =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX : <http://www.semanticweb.org/julian/ontologies/2019/2/untitled-ontology-3#> " +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "SELECT" +
                        " ?title" +
                        " (GROUP_CONCAT(DISTINCT ?year; SEPARATOR=\", \") AS ?years) " +
                        " (GROUP_CONCAT(DISTINCT ?country; SEPARATOR=\", \") AS ?countries) " +
                        " (GROUP_CONCAT(DISTINCT ?genre; SEPARATOR=\" ; \") AS ?genres)" +
                        " (GROUP_CONCAT(DISTINCT ?actor; SEPARATOR=\", \") AS ?actors) " +
                        " (GROUP_CONCAT(DISTINCT ?director; SEPARATOR=\", \") AS ?directors) " +
                        "WHERE { " +
                        "?movie rdf:type owl:NamedIndividual ." +
                        "OPTIONAL {?movie :hasActor ?actor .}" +
                        "?movie :hasTitle ?title ." +
                        "OPTIONAL {?movie :hasGenre ?genre .}" +
                        "OPTIONAL {?movie :hasCountry ?country .}" +
                        "OPTIONAL {?movie :hasDirector ?director .}" +
                        "OPTIONAL {?movie :hasYear ?year .}" +
                        conditions +
                        "}" +
                        "GROUP BY ?title";

        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query,model);
        ResultSet results = qe.execSelect();

        if ((ResultSetFormatter.toList(results).size() == 0))
        {
            System.out.println("No results found.");
        }
        else
        {
            QueryExecution qeBis = QueryExecutionFactory.create(query,model);
            results = qeBis.execSelect();
            QuerySolution querySolution;

            while (results.hasNext())
            {
                querySolution = results.next();
                System.out.println(querySolution);
                String title, actors, years, countries, genre, directors;
                title = querySolution.getLiteral("title").toString();
                try {actors = querySolution.getLiteral("actors").toString();} catch (Exception e){actors = "";}
                try {years = querySolution.getLiteral("years").toString();} catch (Exception e){years = "";}
                try {countries = querySolution.getLiteral("countries").toString();} catch (Exception e){countries = "";}
                try {genre = querySolution.getLiteral("genres").toString();} catch (Exception e){genre = "";}
                try {directors = querySolution.getLiteral("directors").toString();} catch (Exception e){directors = "";}

                ResultTableRow resultTableRow = new ResultTableRow(title, years, countries, genre, actors,directors);
                addRowToResultTable(resultTableRow);
            }

            qeBis.close();
        }
    }

    public void addRowToResultTable(ResultTableRow row)
    {
        ObservableList<ResultTableRow> liste = FXCollections.observableArrayList();
        liste.add(row);
        resultTable.getItems().add(row);
    }
}
