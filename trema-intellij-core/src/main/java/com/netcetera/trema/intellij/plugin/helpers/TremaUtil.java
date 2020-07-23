package com.netcetera.trema.intellij.plugin.helpers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.XMLDatabase;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IExportFilter;
import com.netcetera.trema.core.api.IImportSource;
import com.netcetera.trema.core.exporting.CSVExporter;
import com.netcetera.trema.core.exporting.ExportException;
import com.netcetera.trema.core.exporting.FileOutputStreamFactory;
import com.netcetera.trema.core.exporting.MessageFormatEscapingFilter;
import com.netcetera.trema.core.exporting.PropertiesExporter;
import com.netcetera.trema.core.exporting.TremaCSVPrinter;
import com.netcetera.trema.core.exporting.XLSExporter;
import com.netcetera.trema.core.importing.CSVFile;
import com.netcetera.trema.core.importing.XLSFile;
import com.netcetera.trema.intellij.plugin.models.LanguageAndFileName;
import com.netcetera.trema.intellij.plugin.models.TremaExportModel;
import com.netcetera.trema.intellij.plugin.models.TremaFile;
import com.netcetera.trema.intellij.plugin.models.TremaImportModel;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Utility for performing import/export operations.
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class TremaUtil {

  /**
   * Generate the XML database for the input content.
   * @param input {@link String} content of the source file.
   * @return {@link IDatabase} returns {@link XMLDatabase} the XMLDatabase object from the source.
   * @throws IOException thrown if there is a problem reading the contents.
   * @throws ParseException thrown if there is a problem while parsing the source file contents.
   */
  public static IDatabase getXMLDatabase(String input) throws IOException, ParseException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      FileInputStream inputStream = new FileInputStream(input);
      XMLDatabase db = new XMLDatabase();
      db.build(inputStream, false);
      inputStream.close();
      return db;
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  /**
   * Performs exporting to XLS file.
   * @param model {@link TremaExportModel} model containing export information
   * @param db {@link IDatabase} contains the data to be exported.
   * @throws IOException thrown if there is a problem writing the contents.
   * @throws ExportException thrown if there is a problem while exporting the source file contents.
   */
  public static void doXlsExport(TremaExportModel model, IDatabase db) throws IOException, ExportException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      File output;
      XLSExporter exporter;
      for (LanguageAndFileName langAndFileName: model.getLanguagesToExport()) {
        output = new File(langAndFileName.getFileName());
        output.createNewFile();
        exporter = new XLSExporter(output);
        exporter.export(db.getTextNodes(),
            db.getMasterLanguage(),
            langAndFileName.getLanguage(),
            model.getStatusesToExport().toArray(new Status[model.getStatusesToExport().size()]));
      }
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  /**
   * Performs exporting to CSV file.
   * @param model {@link TremaExportModel} model containing export information
   * @param db {@link IDatabase} contains the data to be exported.
   * @throws IOException thrown if there is a problem writing the contents.
   */
  public static void doCsvExport(TremaExportModel model, IDatabase db) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      OutputStream outputStream = null;
      Writer writer = null;
      CSVExporter exporter;
      for (LanguageAndFileName langAndFileName: model.getLanguagesToExport()) {
        outputStream = new FileOutputStream(langAndFileName.getFileName(), false);
        writer = new OutputStreamWriter(outputStream, model.getCsvOptionsEncoding());
        TremaCSVPrinter printer = new TremaCSVPrinter(writer, model.getCsvOptionsSeparator());
        exporter = new CSVExporter(printer);
        exporter.export(db.getTextNodes(),
            db.getMasterLanguage(),
            langAndFileName.getLanguage(),
            model.getStatusesToExport().toArray(new Status[model.getStatusesToExport().size()]));
      }
      if (writer != null) {
        writer.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  /**
   * Performs exporting to PROPERTIES file.
   * @param model {@link TremaExportModel} model containing export information
   * @param db {@link IDatabase} contains the data to be exported.
   * @throws ExportException thrown if there is a problem while exporting the source file contents.
   */
  public static void doPropertiesExport(TremaExportModel model, IDatabase db) throws ExportException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      PropertiesExporter exporter;

      for (LanguageAndFileName langAndFileName: model.getLanguagesToExport()) {
        exporter = new PropertiesExporter(new File(langAndFileName.getFileName()), new FileOutputStreamFactory());
        if (model.getPropertiesOptionsEscapeQuotes()) {
          exporter.setExportFilter(new IExportFilter[]{new MessageFormatEscapingFilter()});
        }

        exporter.export(db.getTextNodes(),
            null,
            langAndFileName.getLanguage(),
            model.getStatusesToExport().toArray(new Status[model.getStatusesToExport().size()]));
      }
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  /**
   * Performs importing from file. This is called after updating the db object with the imported changes to
   * write to the source file. After writing it refreshes the file contents in IntelliJ.
   * @param model {@link TremaImportModel} model containing import information
   * @param event {@link AnActionEvent} IntelliJ event object
   * @param db {@link IDatabase} contains the data to be exported.
   * @throws IOException thrown if there is a problem writing the contents.
   */
  public static void doImport(XMLDatabase db, AnActionEvent event, TremaImportModel model) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      VirtualFile importDestination = event.getData(CommonDataKeys.VIRTUAL_FILE);
      if (importDestination == null) {
        throw new IOException("Cannot access the import destination.");
      }
      OutputStream outputStream = new FileOutputStream(importDestination.getPath(), false);
      db.writeXML(outputStream, model.getEncoding(), "\t", "\n");
      importDestination.refresh(false, false);
      outputStream.close();
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  /**
   * Method to get the {@link IImportSource} import source from a path.
   * @param path the path of the source file
   * @param encoding the source encoding
   * @param separator the source separator
   * @return Returns {@link XLSFile} or {@link CSVFile} depending on the source.
   * @throws ParseException thrown when there is a problem parsing the source file
   * @throws IOException thrown when there is a problem reading the soruce file
   */
  public static IImportSource getImportSource(String path, String encoding, char separator) throws ParseException,
      IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TremaUtil.class.getClassLoader());
      if (path.endsWith(TremaFile.XLS.getExtension())) {
        return new XLSFile(path);
      }
      if (path.endsWith(TremaFile.CSV.getExtension())) {
        return new CSVFile(path, encoding, separator);
      }
      return null;
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }
}
