import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import ProductDetails from './pages/ProductDetails';
import AddProduct from './pages/AddProduct';
import EditProduct from './pages/EditProduct';
import Wishlist from './pages/Wishlist';
import Orders from './pages/Orders';
import Chat from './pages/Chat';
import AdminDashboard from './pages/AdminDashboard';
import Chatbot from './components/Chatbot';

const isAuthenticated = () => !!localStorage.getItem('token');

const RequireAuth = ({ children }) => (isAuthenticated() ? children : <Navigate to="/login" />);

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <ToastContainer position="top-right" autoClose={3000} hideProgressBar />
        <main className="container mx-auto px-4 py-6">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/product/:id" element={<ProductDetails />} />
            <Route path="/add" element={<RequireAuth><AddProduct /></RequireAuth>} />
            <Route path="/edit/:id" element={<RequireAuth><EditProduct /></RequireAuth>} />
            <Route path="/wishlist" element={<RequireAuth><Wishlist /></RequireAuth>} />
            <Route path="/orders" element={<RequireAuth><Orders /></RequireAuth>} />
            <Route path="/chat" element={<RequireAuth><Chat /></RequireAuth>} />
            <Route path="/dashboard" element={<RequireAuth><AdminDashboard /></RequireAuth>} />
            <Route path="/admin" element={<RequireAuth><AdminDashboard /></RequireAuth>} />
          </Routes>
        </main>
        <Chatbot />
      </div>
    </Router>
  );
}

export default App;

