package be.ucll.workloadplanner;

// Model om de ticket entity te beschrijven
public class Ticket {

    private String ticketId;
    private String title;
    private String info;
    private Boolean finished;

    private User user;

    public Ticket() {
    }

    public Ticket(String ticketId, String title, String info, Boolean finished, User user) {
        this.ticketId = ticketId;
        this.title = title;
        this.info = info;
        this.finished = finished;
        this.user = user;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
