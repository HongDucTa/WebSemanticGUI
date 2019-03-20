package sample;

import javafx.beans.property.SimpleStringProperty;

public class ResultTableRow {
    public SimpleStringProperty title;
    public String year;
    public String country;
    public String genre;
    public String actors;
    public String directors;

    public ResultTableRow(String title,String year,String country,String genre,String actors,String directors) {
        this.title = new SimpleStringProperty(title);
        this.year = year;
        this.country = country;
        this.genre = genre;
        this.actors = actors;
        this.directors = directors;
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getDirectors() {
        return directors;
    }

    public void setDirectors(String directors) {
        this.directors = directors;
    }
}
