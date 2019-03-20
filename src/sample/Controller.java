package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    }

    public void clearFilter()
    {

    }

    public void executeQuery()
    {
        Model model = ModelFactory.createDefaultModel();
        String filePath = new File("").getAbsolutePath();
        // read the TTL file
        model.read(filePath + "\\src\\sample\\final.ttl", null);

        String queryString =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX : <http://www.semanticweb.org/julian/ontologies/2019/2/untitled-ontology-3#>" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "SELECT" +
                        " ?title" +
                        " (GROUP_CONCAT(DISTINCT ?year; SEPARATOR=\", \") AS ?years)" +
                        " (GROUP_CONCAT(DISTINCT ?country; SEPARATOR=\", \") AS ?countries)" +
                        " (GROUP_CONCAT(DISTINCT ?genre; SEPARATOR=\" ; \") AS ?genres)" +
                        " (GROUP_CONCAT(DISTINCT ?actor; SEPARATOR=\", \") AS ?actors) " +
                        " (GROUP_CONCAT(DISTINCT ?director; SEPARATOR=\", \") AS ?directors)" +
                        "WHERE { " +
                        "?x rdf:type owl:NamedIndividual ." +
                        "?x :hasTitle ?title ." +
                        "OPTIONAL {?x :hasCountry ?country .} ." +
                        "OPTIONAL {?x :hasYear ?year .} ." +
                        "OPTIONAL {?x :hasGenre ?genre .} ." +
                        "OPTIONAL {?x :hasActor ?actor .}" +
                        "}" +
                        "GROUP BY ?title";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query,model);
        ResultSet results = qe.execSelect();

        if ((ResultSetFormatter.toList(results).get(0)).toString().equals(""))
        {
            System.out.println("No results found.");
        }
        else
        {
            QueryExecution qeBis = QueryExecutionFactory.create(query,model);
            results = qeBis.execSelect();
            ResultSetFormatter.out(System.out,results,query);
            qeBis.close();
        }

        ResultTableRow row = new ResultTableRow("Kill Bill (volume1)","2003","USA","Thriller, Crime, Action","Georges","Mark");
        addRowToResultTable(row);
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
