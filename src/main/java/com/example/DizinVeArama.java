package com.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DizinVeArama {
    public static void main(String[] args) throws Exception {
        // Analizör ve FSDirectory ile bir dizin oluşturma
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get("./index"));

        // Dizin yazarı başlatılıyor
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        // Veri ekleme
        ekleDokuman(writer, 2, "Pınar", 20, "Erkan", "Memur", "Destekleyici", "Sohbet", "Yüksek", "Güçlü", "Günlük", "Güven dolu", "Fazla eleştiri", "Aile terapisi");
        ekleDokuman(writer, 3, "Sema", 18, "Musa", "Doktor", "Destekleyici değil", "Yok", "Düşük", "Zayıf", "Aylık", "Nefret", "Laf atma, Fazla eleştiri", "Aile terapisi");
        ekleDokuman(writer, 4, "İkbal", 19, "Hakan", "Subay", "Destekleyici değil", "Yok", "Düşük", "Zayıf", "Yıllık", "Belirsiz", "Psikolojik", "Aile terapisi");
        ekleDokuman(writer, 1, "Binhan", 22, "Erkan", "Memur", "Destekleyici", "Film izlemek, Sohbet", "Yüksek", "Güçlü", "Günlük", "Güven dolu", "Eleştiri", "Aile terapisi");

        // Dizin yazma işlemini kapatma
        writer.close();

        // Dizin okuyucu ve sorgulayıcı başlatma
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // 1. Sorgu: Tek bir kategoride arama
        sorguYap1(searcher, analyzer, "baba_meslegi", "Memur");

        // 2. Sorgu: İki farklı kategoride arama
        sorguYap(searcher, analyzer, "duygusal_baglanma", "Zayıf", "baba_yaklasimi", "Destekleyici değil");

        // 3. Sorgu: İki kategori ve bir tersten sorgu
        sorguYap(searcher, analyzer, "baba_yaklasimi", "Destekleyici değil", "duygusal_baglanma", "Zayıf");

        // Dizin okuyucuyu kapatma
        reader.close();
    }

    // Bir belge eklemek için yardımcı fonksiyon
    private static void ekleDokuman(IndexWriter writer, Integer kayitId, String cocukAdi, Integer cocukYasi, String babaAdi, String babaMeslegi, String babaYaklasimi, String ortakAktiviteler, String iletisimSeviyesi, String duygusalBaglanma, String gorusmeFrekansi, String cocukDuygulari, String problemDurumlari, String problemCozmeYontemleri) throws Exception {
        Document doc = new Document();
        doc.add(new TextField("kayit_id", Integer.toString(kayitId), Field.Store.YES));
        doc.add(new TextField("cocuk_adi", cocukAdi, Field.Store.YES));
        doc.add(new TextField("cocuk_yasi", Integer.toString(cocukYasi), Field.Store.YES));
        doc.add(new TextField("baba_adi", babaAdi, Field.Store.YES));
        doc.add(new TextField("baba_meslegi", babaMeslegi, Field.Store.YES));
        doc.add(new TextField("baba_yaklasimi", babaYaklasimi, Field.Store.YES));
        doc.add(new TextField("ortak_aktiviteler", ortakAktiviteler, Field.Store.YES));
        doc.add(new TextField("iletisim_seviyesi", iletisimSeviyesi, Field.Store.YES));
        doc.add(new TextField("duygusal_baglanma", duygusalBaglanma, Field.Store.YES));
        doc.add(new TextField("gorusme_frekansi", gorusmeFrekansi, Field.Store.YES));
        doc.add(new TextField("cocuk_duygulari", cocukDuygulari, Field.Store.YES));
        doc.add(new TextField("problem_durumlari", problemDurumlari, Field.Store.YES));
        doc.add(new TextField("problem_cozme_yontemleri", problemCozmeYontemleri, Field.Store.YES));
        writer.addDocument(doc);
    }

    private static void sorguYap(IndexSearcher searcher, StandardAnalyzer analyzer, String field1, String queryString1, String field2, String queryString2) throws Exception {
        // BooleanQuery kullanarak sorguları birleştirme
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        QueryParser parser1 = new QueryParser(field1, analyzer);
        Query query1 = parser1.parse(queryString1);
        booleanQueryBuilder.add(query1, BooleanClause.Occur.MUST);

        QueryParser parser2 = new QueryParser(field2, analyzer);
        Query query2 = parser2.parse(queryString2);
        booleanQueryBuilder.add(query2, BooleanClause.Occur.MUST);

        Query finalQuery = booleanQueryBuilder.build();

        // Sorgu sonuçları
        System.out.println("---------------------------");
        System.out.println("Yapılan Sorgu: " + field1 + "='" + queryString1 + "' Ve " + field2 + "='" + queryString2 + "'");
        ScoreDoc[] hits = searcher.search(finalQuery, 10).scoreDocs;

        Set<String> foundNames = new HashSet<>();

        if (hits.length > 0) {
            for (ScoreDoc hit : hits) {
                Document d = searcher.doc(hit.doc);
                String cocukAdi = d.get("cocuk_adi");

                // Eğer çocuk daha önce yazdırılmamışsa, ekleyip yazdırıyoruz
                if (!foundNames.contains(cocukAdi)) {
                    foundNames.add(cocukAdi);
                    System.out.println("Bulunan Çocuk: " + cocukAdi);
                }
            }
        } else {
            System.out.println("Sonuç bulunamadı.");
        }
    }

    private static void sorguYap1(IndexSearcher searcher, StandardAnalyzer analyzer, String field, String queryString) throws Exception {
        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryString);

        // Sorgu sonuçları
        System.out.println("---------------------------");
        System.out.println("Yapılan Sorgu: " + field + "='" + queryString + "'");
        ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;

        Set<String> foundNames = new HashSet<>();
        
        if (hits.length > 0) {
            for (ScoreDoc hit : hits) {
                Document d = searcher.doc(hit.doc);
                String cocukAdi = d.get("cocuk_adi");

                // Eğer çocuk daha önce yazdırılmamışsa, ekleyip yazdırıyoruz
                if (!foundNames.contains(cocukAdi)) {
                    foundNames.add(cocukAdi);
                    System.out.println("Bulunan Çocuk: " + cocukAdi);
                }
            }
        } else {
            System.out.println("Sonuç bulunamadı.");
        }
    }
}
