import random

first_names = ['Amit', 'Raj', 'Priya', 'Neha', 'Sanjay', 'Vikram', 'Anita', 'Sunil', 'Kiran', 'Pooja', 'Ravi', 'Manoj', 'Ritu', 'Kavita', 'Anil', 'Deepak', 'Suresh', 'Ramesh', 'Geeta', 'Nisha', 'Vijay', 'Ajay', 'Rakesh', 'Sita', 'Gita', 'Rahul', 'Rohit', 'Sneha', 'Vivek', 'Sachin', 'Anjali', 'Arun', 'Tarun', 'Varun', 'Nitin', 'Divya', 'Shilpa', 'Vikas', 'Swati', 'Preeti', 'Prakash', 'Mahesh', 'Ganesh', 'Karthik', 'Asha', 'Usha', 'Sushma', 'Smriti', 'Kirti', 'Meghna', 'Alok', 'Ashish', 'Naveen', 'Praveen', 'Pradeep', 'Sandeep', 'Sumit', 'Amitabh', 'Abhishek', 'Aishwarya', 'Kareena', 'Karishma', 'Salman', 'Shahrukh', 'Aamir', 'Hrithik', 'Ranbir', 'Ranveer', 'Deepika', 'Alia', 'Katrina', 'Anushka', 'Shraddha', 'Kriti', 'Kiara', 'Disha', 'Tiger', 'Varun', 'Siddharth', 'Ayushmann', 'Rajkummar', 'Vicky', 'Taapsee', 'Bhumi', 'Vidya', 'Kangana', 'Priyanka', 'Nick', 'Virat', 'MS', 'Rohit', 'Shikhar', 'KL', 'Hardik', 'Jasprit', 'Ravindra', 'Rishabh', 'Shreyas', 'Manish', 'Krunal', 'Navdeep', 'Deepak', 'Shardul', 'Washington', 'Axar', 'Rahul', 'Ishan', 'Suryakumar', 'Prithvi', 'Shubman', 'Mayank', 'Cheteshwar', 'Ajinkya', 'Hanuma', 'Ravi', 'Umesh', 'Ishant', 'Mohammed']
last_names = ['Sharma', 'Verma', 'Gupta', 'Singh', 'Kumar', 'Patil', 'Deshmukh', 'Joshi', 'Kulkarni', 'Desai', 'Patel', 'Shah', 'Mehta', 'Trivedi', 'Vyas', 'Bhatt', 'Rao', 'Reddy', 'Choudhary', 'Yadav', 'Pandey', 'Mishra', 'Tiwari', 'Dubey', 'Shukla', 'Agnihotri', 'Chaturvedi', 'Srivastava', 'Saxena', 'Mathur', 'Bhatnagar', 'Agarwal', 'Garg', 'Bansal', 'Goyal', 'Mittal', 'Jindal', 'Singhal', 'Kansal', 'Tayal', 'Ahluwalia', 'Chawla', 'Bhatia', 'Kapur', 'Malhotra', 'Khanna', 'Mehra', 'Chopra', 'Sethi', 'Kohli', 'Suri', 'Oberoi', 'Tandon', 'Bhasin', 'Madan', 'Nanda', 'Khullar', 'Dhawan', 'Sehgal', 'Grover', 'Narula', 'Kalra', 'Arora', 'Khurana', 'Makkar', 'Batra', 'Talwar', 'Chhabra', 'Gulati', 'Narang', 'Lamba', 'Sood', 'Bhagat', 'Dutt', 'Basu', 'Ghosh', 'Bose', 'Mitra', 'Sen', 'Das', 'Roy', 'Chakraborty', 'Banerjee', 'Chatterjee', 'Mukherjee', 'Bhattacharya', 'Ganguly', 'Nair', 'Menon', 'Pillai', 'Kurian', 'Verghese', 'Oommen', 'Iyer', 'Iyengar', 'Rao', 'Prabhu', 'Shenoy', 'Kamat', 'Nayak', 'Kini', 'Pai', 'Bhat', 'Hegde', 'Gowda', 'Shetty', 'Poojary', 'Rai', 'Alva', 'Hegde', 'Naidu', 'Raju', 'Varma']

sql_accounts = []
sql_users = []

account_types = ['SAVINGS', 'CURRENT']

for i in range(1150, 2650):
    fn = random.choice(first_names)
    ln = random.choice(last_names)
    name = f'{fn} {ln}'
    acc_id = f'ACC{i}'
    acc_type = random.choice(account_types)
    balance = round(random.uniform(500.0, 5000000.0), 2)
    sql_accounts.append(f"('{acc_id}', '{acc_type}', '{name}', {balance})")
    
    username = f'{fn.lower()}{random.randint(10,99)}'
    password = 'pass' + str(random.randint(100,999))
    sql_users.append(f"('{username}', '{password}', '{name}', 'CUSTOMER', '{acc_id}')")

with open('src/main/resources/data.sql', 'a') as f:
    f.write('\n\n-- Generated Users (129 extra for realistic demo) --\n')
    f.write('INSERT INTO bank_account (account_id, account_type, account_holder_name, balance) VALUES\n')
    f.write(',\n'.join(sql_accounts) + ';\n\n')
    f.write('INSERT INTO users (username, password, full_name, role, account_id) VALUES\n')
    f.write(',\n'.join(sql_users) + ';\n')

print("Data appended to data.sql successfully.")
