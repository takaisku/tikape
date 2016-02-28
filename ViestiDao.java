
/**
 *
 * @author tkarkine
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import org.jsoup.Jsoup;

public class ViestiDao {
    private Connection yhteys;
    
    public ViestiDao(String db) throws SQLException {
        try {
            yhteys= DriverManager.getConnection("jdbc:sqlite:"+db);
            System.out.println("Yhteys tietokantaan toimii!");
        } catch (Exception e){
            System.out.println("Virhe " + e);
        }
        
    }
    
    public List<Alue> getAlueet() throws SQLException {
        List<Alue> rivit = new ArrayList<>();
        String kysely="SELECT Alue.nimi, count(Viesti.viestiId), max(Viesti,aika) From Viesti " 
                + "join Ketju on Viesti.ketjuId=Ketju.ketjuId "
                + "join Alue on Ketju.alueId=Alue.alueId"
                + "group by Alue.alueID order by Alue.nimi ASC;";
        
        
        PreparedStatement stmt = yhteys.prepareStatement(kysely);
        
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()){
        String nimi=rs.getString(1);
        int lkm=rs.getInt(2);
        Timestamp aika=rs.getTimestamp(3);
        
        rivit.add(new Alue(nimi,lkm,aika));
        }
        stmt.close();
        rs.close();
        return rivit;
    }
    
    public List<Ketju> getKetjut(int alueId, int offSet) throws SQLException {
        List<Ketju> rivit = new ArrayList<>();
        String kysely="SELECT Ketju.avaus, count(Viesti.viestiId), max(Viesti,aika) as uusin From Viesti " 
                + "join Ketju on Viesti.ketjuId=Ketju.ketjuId "
                + "join Alue on Ketju.alueId=Alue.alueId"
                + "where Alue.alueid= ?"
                + "group by Ketju.ketjuID order by uusin DESC LIMIT 10 OFFSET ?;";
        
        
        PreparedStatement stmt = yhteys.prepareStatement(kysely);
        stmt.setInt(1, alueId);
        stmt.setInt(2, offSet);
        
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()){
        String avaus=rs.getString(1);
        int lkm=rs.getInt(2);
        Timestamp aika=rs.getTimestamp(3);
        
        rivit.add(new Ketju(avaus,lkm,aika));
        }
        stmt.close();
        rs.close();
        return rivit;
    }
    
    public List<Viesti> getViestit(int ketjuId, int offSet) throws SQLException {
        List<Viesti> rivit = new ArrayList<>();
        String kysely="SELECT * From Viesti " 
                + "where Viesti.ketjuId= ?"
                + "order by Viesti.aika DESC LIMIT 10 OFFSET ?;";
        
        
        PreparedStatement stmt = yhteys.prepareStatement(kysely);
        stmt.setInt(1, ketjuId);
        stmt.setInt(2, offSet);
        
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()){
        int id=rs.getInt(1);
        int ketju=rs.getInt(2);
        int kayttaja=rs.getInt(3);
        Timestamp aika=rs.getTimestamp(4);
        String teksti=rs.getString(5);
        
        rivit.add(new Viesti(id,ketju,kayttaja,aika,teksti));
        }
        stmt.close();
        rs.close();
        return rivit;
    }
    
    public boolean setViesti(int ketjuId, int kayttaja,String teksti) throws SQLException{
        String kysely="INSERT INTO Viesti " 
                + "(ketjuId,kayttajaId,aika,teksti)"
                + "VALUES (?,?,?,?);";
        
        
        PreparedStatement stmt = yhteys.prepareStatement(kysely);
        stmt.setInt(1, ketjuId);
        stmt.setInt(2, kayttaja);
        stmt.setTimestamp(3, new Timestamp(Calendar.getInstance().getTime().getTime()));
        stmt.setString(4, Jsoup.parse(teksti).text());
        
        boolean palaute = stmt.execute();
        stmt.close();
        return palaute;
    }
}

