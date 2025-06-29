#!/usr/bin/env python3
"""
Network Helper Script untuk Tubes-RPLL API
Script ini membantu menemukan IP address lokal dan menguji konektivitas API
"""

import socket
import subprocess
import platform
import requests
import time
from flask import Flask

def get_local_ip():
    """Mendapatkan IP address lokal"""
    try:
        # Metode 1: Menggunakan socket
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception:
        try:
            # Metode 2: Menggunakan hostname
            hostname = socket.gethostname()
            local_ip = socket.gethostbyname(hostname)
            return local_ip
        except Exception:
            return "127.0.0.1"

def get_all_network_interfaces():
    """Mendapatkan semua network interfaces"""
    interfaces = []
    try:
        if platform.system() == "Windows":
            result = subprocess.run(['ipconfig'], capture_output=True, text=True, shell=True)
            lines = result.stdout.split('\n')
            current_adapter = ""
            
            for line in lines:
                line = line.strip()
                if "adapter" in line.lower() and ":" in line:
                    current_adapter = line
                elif "IPv4 Address" in line and ":" in line:
                    ip = line.split(":")[-1].strip()
                    if ip and not ip.startswith("127."):
                        interfaces.append({
                            'adapter': current_adapter,
                            'ip': ip
                        })
        else:
            # Linux/Mac
            result = subprocess.run(['ifconfig'], capture_output=True, text=True)
            # Parsing untuk Linux/Mac bisa ditambahkan di sini
            pass
            
    except Exception as e:
        print(f"Error getting network interfaces: {e}")
    
    return interfaces

def test_api_connectivity(ip_address, port=5000):
    """Test konektivitas ke API"""
    try:
        url = f"http://{ip_address}:{port}/"
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            return True, "API accessible"
        else:
            return False, f"API returned status code: {response.status_code}"
    except requests.exceptions.ConnectionError:
        return False, "Connection refused - API might not be running or firewall blocking"
    except requests.exceptions.Timeout:
        return False, "Connection timeout"
    except Exception as e:
        return False, f"Error: {str(e)}"

def check_firewall_status():
    """Check Windows Firewall status"""
    try:
        if platform.system() == "Windows":
            result = subprocess.run([
                'netsh', 'advfirewall', 'firewall', 'show', 'rule', 
                'name=all', 'dir=in', 'protocol=tcp', 'localport=5000'
            ], capture_output=True, text=True, shell=True)
            
            if "No rules match" in result.stdout:
                return False, "No firewall rule found for port 5000"
            else:
                return True, "Firewall rule exists for port 5000"
        else:
            return None, "Firewall check only available on Windows"
    except Exception as e:
        return None, f"Error checking firewall: {e}"

def main():
    print("🌐 Tubes-RPLL API Network Helper")
    print("=" * 50)
    
    # 1. Mendapatkan IP address lokal
    local_ip = get_local_ip()
    print(f"📍 IP Address Utama: {local_ip}")
    
    # 2. Mendapatkan semua network interfaces
    print("\n🔍 Semua Network Interfaces:")
    interfaces = get_all_network_interfaces()
    if interfaces:
        for i, interface in enumerate(interfaces, 1):
            print(f"   {i}. {interface['adapter']}")
            print(f"      IP: {interface['ip']}")
    else:
        print(f"   Hanya ditemukan: {local_ip}")
    
    # 3. Test konektivitas API
    print(f"\n🧪 Testing API Connectivity...")
    is_accessible, message = test_api_connectivity(local_ip)
    
    if is_accessible:
        print(f"✅ API dapat diakses di: http://{local_ip}:5000")
    else:
        print(f"❌ API tidak dapat diakses: {message}")
    
    # 4. Check firewall
    print(f"\n🔥 Checking Windows Firewall...")
    firewall_status, firewall_message = check_firewall_status()
    
    if firewall_status is True:
        print(f"✅ {firewall_message}")
    elif firewall_status is False:
        print(f"⚠️  {firewall_message}")
        print("   Jalankan setup_firewall.bat sebagai Administrator untuk membuka port")
    else:
        print(f"ℹ️  {firewall_message}")
    
    # 5. Instruksi untuk laptop lain
    print(f"\n📱 Untuk mengakses dari laptop lain:")
    print(f"   1. Pastikan laptop lain terhubung ke WiFi/jaringan yang sama")
    print(f"   2. Buka browser atau aplikasi dan gunakan URL:")
    print(f"      http://{local_ip}:5000")
    print(f"   3. Untuk test API, gunakan:")
    print(f"      http://{local_ip}:5000/api/status")
    
    # 6. Troubleshooting
    print(f"\n🔧 Troubleshooting:")
    print(f"   - Jika tidak bisa diakses, jalankan setup_firewall.bat sebagai Admin")
    print(f"   - Pastikan API server sedang berjalan (python app.py)")
    print(f"   - Cek apakah antivirus memblokir koneksi")
    print(f"   - Pastikan router tidak memblokir komunikasi antar device")

if __name__ == "__main__":
    main() 