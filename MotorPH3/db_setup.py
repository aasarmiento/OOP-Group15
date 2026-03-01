import sqlite3
import pandas as pd
import os

# 1. Connect to (or create) the database file
conn = sqlite3.connect('my_database.db')

# 2. Path to your CSV folder
csv_folder = './data'

# 3. Loop through every CSV and turn it into a SQL Table
for filename in os.listdir(csv_folder):
    if filename.endswith('.csv'):
        file_path = os.path.join(csv_folder, filename)
        
        # Clean the name for the table (e.g., 'users.csv' becomes 'users')
        table_name = os.path.splitext(filename)[0]
        
        # Load CSV and save to SQL
        df = pd.read_csv(file_path)
        df.to_sql(table_name, conn, if_exists='replace', index=False)
        print(f"✅ Imported {filename} as table [{table_name}]")

conn.close()