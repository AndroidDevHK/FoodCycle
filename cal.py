import os

def count_lines_and_files(directory, extension):
    total_lines = 0
    total_files = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(extension):
                total_files += 1
                try:
                    with open(os.path.join(root, file), 'r', encoding='utf-8') as f:
                        total_lines += sum(1 for line in f)
                except (UnicodeDecodeError, IOError) as e:
                    print(f"Error reading {file}: {e}")
    return total_files, total_lines

directory = r"C:\Users\Hasnat\Desktop\foodcycle"

java_files, java_lines = count_lines_and_files(directory, ".java")
xml_files, xml_lines = count_lines_and_files(directory, ".xml")
total_lines = java_lines + xml_lines

print(f"Total .java files: {java_files}")
print(f"Total lines of code in .java files: {java_lines}")
print(f"Total .xml files: {xml_files}")
print(f"Total lines of code in .xml files: {xml_lines}")
print(f"Total lines of code in both .java and .xml files: {total_lines}")
