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
        executeQuery();
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
        for (int i = 0;i < conditionsList.size();i++)
        {
            row = "?x ";
            criteria = conditionsList.get(i).getCriteria();
            criteriaValue = conditionsList.get(i).getValue();
            includeCriteria = conditionsList.get(i).getIncludeOrExclude();
            switch (criteria)
            {
                case "Actor":
                    row = row + ":hasActor ";
                    break;
                case "Director":
                    row = row + ":hasDirector ";
                    break;
                case "Genre":
                    row = row + ":hasGenre ";
                default:
                    break;
            }
            row = row + criteriaValue;

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
        criteriaTable.getItems().remove(0,criteriaTable.getItems().size());
    }

    public void executeQuery()
    {
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
                        "?x rdf:type owl:NamedIndividual ." +
                        "?x :hasActor ?actor ." +
                        "?x :hasTitle ?title ." +
                        "?x :hasGenre ?genre ." +
                        "?x :hasCountry ?country ." +
                        "?x :hasDirector ?dirtector" +
                        "?x :hasActor :georgeClooney ." +
                        "}" +
                        "GROUP BY ?title";
/*
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
                        "?x rdf:type owl:NamedIndividual ." +
                        "?x :hasActor ?actor ." +
                        "?x :hasTitle ?title ." +
                        conditions +
                        "}" +
                        "GROUP BY ?title";
*/

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
            //ResultSetFormatter.out(System.out,results,query);
            QuerySolution querySolution = results.next();
            System.out.println(querySolution);
            String title,actors,years,countries,genre,directors;
            title = querySolution.getLiteral("title").toString();
            actors = querySolution.getLiteral("actors").toString();
            //years = querySolution.getLiteral("years").toString();
            //countries = querySolution.getLiteral("countries").toString();
            //genre = querySolution.getLiteral("genres").toString();
            //directors = querySolution.getLiteral("directors").toString();

            //ResultTableRow resultTableRow = new ResultTableRow(title,years,countries,genre,actors,directors);
            //addRowToResultTable(resultTableRow);

            qeBis.close();
        }

        //ResultTableRow row = new ResultTableRow("Kill Bill (volume1)","2003","USA","Thriller, Crime, Action","Georges","Mark");
    }

    public void addRowToResultTable(ResultTableRow row)
    {
        ObservableList<ResultTableRow> liste = FXCollections.observableArrayList();
        liste.add(row);
        resultTable.getItems().add(row);
        resultTable.getItems().addAll(liste);
        resultTable.getItems().addAll(liste);
        resultTable.getItems().addAll(liste);

    }
}
