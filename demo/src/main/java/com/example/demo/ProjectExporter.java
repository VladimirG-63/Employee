package com.example.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectExporter {
    private static final List<String> IGNORE_DIRS = Arrays.asList(".git", ".idea", "target", "out");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".java", ".xml", ".properties", ".jsp", ".html", ".md");

    public static void main(String[] args) {
        Path projectDir = Paths.get(".");
        Path outputFile = Paths.get("project_context.txt");

        // Явно указываем UTF-8 для записи файла
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("=== ИНФОРМАЦИЯ О ПРОЕКТЕ ===\n\n");

            writer.write("--- СТРУКТУРА ПАПОК И ФАЙЛОВ ---\n");
            printTree(projectDir, "", true, writer);
            writer.write("\n\n");

            writer.write("--- ИСПОЛЬЗУЕМЫЕ БИБЛИОТЕКИ (MAVEN) ---\n");
            appendMavenDependencies(writer);
            writer.write("\n\n");

            writer.write("--- ИСХОДНЫЙ КОД ФАЙЛОВ ---\n\n");
            appendFileContents(projectDir, writer);

            System.out.println("Успех! Файл создан: " + outputFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Произошла ошибка при создании файла: " + e.getMessage());
        }
    }

    private static void printTree(Path dir, String prefix, boolean isTail, BufferedWriter writer) {
        try {
            String dirName = dir.getFileName() != null ? dir.getFileName().toString() : dir.toString();
            writer.write(prefix + (isTail ? "└── " : "├── ") + dirName + "\n");

            try (Stream<Path> paths = Files.list(dir)) {
                List<Path> children = paths
                        .filter(p -> {
                            String name = p.getFileName().toString();
                            if (Files.isDirectory(p)) return !IGNORE_DIRS.contains(name);
                            return true;
                        })
                        .sorted((a, b) -> {
                            if (Files.isDirectory(a) && !Files.isDirectory(b)) return -1;
                            if (!Files.isDirectory(a) && Files.isDirectory(b)) return 1;
                            return a.getFileName().compareTo(b.getFileName());
                        })
                        .collect(Collectors.toList());

                for (int i = 0; i < children.size() - 1; i++) {
                    printTree(children.get(i), prefix + (isTail ? "    " : "│   "), false, writer);
                }
                if (!children.isEmpty()) {
                    printTree(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true, writer);
                }
            }
        } catch (IOException e) {
            try { writer.write(prefix + "    [Нет доступа к папке]\n"); } catch (IOException ignored) {}
        }
    }

    private static void appendMavenDependencies(BufferedWriter writer) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String mvnCommand = isWindows ? "mvn.cmd" : "mvn";

            ProcessBuilder pb = new ProcessBuilder(mvnCommand, "dependency:tree");
            pb.directory(new File("."));
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean isTreePart = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("maven-dependency-plugin")) {
                        isTreePart = true;
                        continue;
                    }
                    if (line.contains("BUILD SUCCESS") || line.contains("BUILD FAILURE")) {
                        isTreePart = false;
                    }
                    if (isTreePart && line.startsWith("[INFO]")) {
                        writer.write(line.replace("[INFO]", "").trim() + "\n");
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            try {
                writer.write("Не удалось автоматически получить дерево зависимостей.\n");
            } catch (IOException ignored) {}
        }
    }

    private static void appendFileContents(Path projectDir, BufferedWriter writer) throws IOException {
        Files.walkFileTree(projectDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName() != null && IGNORE_DIRS.contains(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName() == null) return FileVisitResult.CONTINUE;
                String fileName = file.getFileName().toString();
                boolean isAllowed = ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);

                if (isAllowed) {
                    writer.write("==================================================\n");
                    writer.write("ФАЙЛ: " + projectDir.relativize(file) + "\n");
                    writer.write("==================================================\n");

                    try {
                        // Сначала пробуем прочитать в современной кодировке UTF-8
                        writer.write(Files.readString(file, StandardCharsets.UTF_8) + "\n\n");
                    } catch (Exception e1) {
                        try {
                            // Если не вышло (например, файл в Windows-1251), пробуем системную кодировку
                            writer.write(Files.readString(file) + "\n\n");
                        } catch (Exception e2) {
                            writer.write("<< Не удалось прочитать код: ошибка кодировки >>\n\n");
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            // МАГИЯ ЗДЕСЬ: Перехватываем ошибки доступа к файлам и не даем скрипту упасть
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}