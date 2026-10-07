interface Report { String render(); }
class StatusReport implements Report {
    public String render() { return "Rapport: prêt"; }
}
public class Main {
    public static void main(String[] args) {
        Report report = new StatusReport();
        System.out.println(report.render());
    }
}
