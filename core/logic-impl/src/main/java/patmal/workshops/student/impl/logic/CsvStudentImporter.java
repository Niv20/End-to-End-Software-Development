package patmal.workshops.student.impl.logic;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Student;

/**
 * Reads students from a CSV file with the columns id, name, email.
 * Every line is registered with createStudent, so the same rules apply. The import stops at the
 * first rejected line; the students of the lines before it stay registered.
 */
class CsvStudentImporter {

    private static final List<String> HEADER = List.of("id", "name", "email");
    private static final String HEADER_LINE = String.join(",", HEADER);

    // Empty lines are skipped and values may be quoted (the defaults of CSVFormat.DEFAULT)
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setIgnoreSurroundingSpaces(true)
            .get();

    List<Student> importStudents(File source, WorkshopSystem system) {
        if (source == null) {
            throw new IllegalArgumentException("The source file must not be null");
        }
        try (Reader reader = Files.newBufferedReader(source.toPath(), StandardCharsets.UTF_8);
             CSVParser parser = FORMAT.parse(reader)) {
            return readStudents(parser, system);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the file " + source, e);
        } catch (UncheckedIOException e) {
            // commons-csv reports a broken line (for example a quote that is not closed) this way
            if (e.getCause() instanceof CSVException) {
                throw new IllegalArgumentException("The file " + source + " is not a valid CSV file: "
                        + e.getCause().getMessage(), e);
            }
            throw e;
        }
    }

    private List<Student> readStudents(CSVParser parser, WorkshopSystem system) {
        Iterator<CSVRecord> lines = parser.iterator();
        if (!lines.hasNext()) {
            throw new IllegalArgumentException("The file is empty, the header line " + HEADER_LINE + " is missing");
        }
        List<String> header = lines.next().toList();
        if (!header.equals(HEADER)) {
            throw new IllegalArgumentException("The header line must be " + HEADER_LINE
                    + " but was " + String.join(",", header));
        }

        List<Student> students = new ArrayList<>();
        while (lines.hasNext()) {
            CSVRecord line = lines.next();
            if (isBlank(line)) {
                continue;
            }
            students.add(createStudent(line, parser.getCurrentLineNumber(), system));
        }
        return students;
    }

    private Student createStudent(CSVRecord line, long lineNumber, WorkshopSystem system) {
        if (line.size() != HEADER.size()) {
            throw new IllegalArgumentException("Line " + lineNumber + ": expected " + HEADER.size()
                    + " values but found " + line.size());
        }
        try {
            return system.createStudent(line.get(0), line.get(1), line.get(2));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    // A line with only spaces is read as one empty value
    private boolean isBlank(CSVRecord line) {
        return line.size() == 1 && line.get(0).isEmpty();
    }
}
