import React, { useEffect, useState } from 'react';
import api from '../api';
import ProductCard from '../components/ProductCard';
import CategoryFilter from '../components/CategoryFilter';

const Home = () => {
  const [products, setProducts] = useState([]);
  const [category, setCategory] = useState('ALL');
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      if (keyword) {
        const res = await api.get('products/search', { params: { keyword } });
        setProducts(res.data);
        return;
      }

      if (category !== 'ALL') {
        const res = await api.get(`products/category/${category}`);
        setProducts(res.data);
        return;
      }

      const res = await api.get('products');
      setProducts(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [category, keyword]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Campus Marketplace</h1>
        <p className="text-gray-600">Buy and sell products within your campus community</p>
      </div>

      {/* Search and Filter Section */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <input
                type="text"
                value={keyword}
                placeholder="Search for products..."
                onChange={(e) => setKeyword(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
              />
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
            </div>
          </div>
          <div className="md:w-64">
            <CategoryFilter value={category} onChange={setCategory} />
          </div>
        </div>
      </div>

      {/* Products Grid */}
      {loading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      ) : (
        <>
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold text-gray-900">
              {keyword ? `Search results for "${keyword}"` : category !== 'ALL' ? `${category} Products` : 'All Products'}
            </h2>
            <span className="text-sm text-gray-500">{products.length} products found</span>
          </div>

          {products.length === 0 ? (
            <div className="text-center py-12">
              <svg className="mx-auto h-24 w-24 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2M4 13h2m8-5v2m0 0v2m0-2h2m-2 0h-2" />
              </svg>
              <h3 className="mt-4 text-lg font-medium text-gray-900">No products found</h3>
              <p className="mt-2 text-gray-500">
                {keyword ? 'Try adjusting your search terms.' : 'Be the first to list a product!'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} onRefresh={fetchProducts} />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Home;
