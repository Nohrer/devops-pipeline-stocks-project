import React, { useState, useEffect } from 'react';
import { stockAPI } from '../api/api';

const StockList = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingStock, setEditingStock] = useState(null);
  const [formData, setFormData] = useState({
    date: '',
    openValue: '',
    highValue: '',
    lowValue: '',
    closeValue: '',
    volume: '',
    companyId: '',
  });

  useEffect(() => {
    fetchStocks();
  }, []);

  const fetchStocks = async () => {
    try {
      setLoading(true);
      console.log('Fetching stocks...');
      console.log('Token:', localStorage.getItem('access_token') ? 'Present' : 'Missing');
      const response = await stockAPI.getAll();
      console.log('Stocks fetched successfully:', response.data);
      setStocks(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching stocks:', err);
      console.error('Error response:', err.response);
      const errorMsg = err.response?.data?.message || err.message || 'Failed to fetch stocks';
      setError(`Failed to fetch stocks: ${errorMsg}. Please make sure you are authenticated.`);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        openValue: parseFloat(formData.openValue),
        highValue: parseFloat(formData.highValue),
        lowValue: parseFloat(formData.lowValue),
        closeValue: parseFloat(formData.closeValue),
        volume: parseInt(formData.volume),
        companyId: formData.companyId ? parseInt(formData.companyId) : null,
      };

      if (editingStock) {
        await stockAPI.update(editingStock.id, payload);
      } else {
        await stockAPI.create(payload);
      }
      
      resetForm();
      fetchStocks();
    } catch (err) {
      setError('Failed to save stock. Make sure you have ADMIN role.');
      console.error('Error saving stock:', err);
    }
  };

  const handleEdit = (stock) => {
    setEditingStock(stock);
    setFormData({
      date: stock.date,
      openValue: stock.openValue,
      highValue: stock.highValue,
      lowValue: stock.lowValue,
      closeValue: stock.closeValue,
      volume: stock.volume,
      companyId: stock.companyId || '',
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this stock?')) {
      try {
        await stockAPI.delete(id);
        fetchStocks();
      } catch (err) {
        setError('Failed to delete stock. Make sure you have ADMIN role.');
        console.error('Error deleting stock:', err);
      }
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setEditingStock(null);
    setFormData({
      date: '',
      openValue: '',
      highValue: '',
      lowValue: '',
      closeValue: '',
      volume: '',
      companyId: '',
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-xl text-gray-600">Loading stocks...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Stock Market Quotations</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition duration-300"
        >
          {showForm ? 'Cancel' : 'Add Stock'}
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {showForm && (
        <div className="bg-white shadow-md rounded-lg p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">
            {editingStock ? 'Edit Stock' : 'Add New Stock'}
          </h2>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-gray-700 font-medium mb-2">Company ID (Optional)</label>
              <input
                type="number"
                value={formData.companyId}
                onChange={(e) => setFormData({ ...formData, companyId: e.target.value })}
                placeholder="Leave empty for random ID"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <small className="text-gray-500">If empty, a random ID will be assigned</small>
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">Date</label>
              <input
                type="date"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">Open Value</label>
              <input
                type="number"
                step="0.01"
                value={formData.openValue}
                onChange={(e) => setFormData({ ...formData, openValue: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">High Value</label>
              <input
                type="number"
                step="0.01"
                value={formData.highValue}
                onChange={(e) => setFormData({ ...formData, highValue: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">Low Value</label>
              <input
                type="number"
                step="0.01"
                value={formData.lowValue}
                onChange={(e) => setFormData({ ...formData, lowValue: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">Close Value</label>
              <input
                type="number"
                step="0.01"
                value={formData.closeValue}
                onChange={(e) => setFormData({ ...formData, closeValue: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-gray-700 font-medium mb-2">Volume</label>
              <input
                type="number"
                value={formData.volume}
                onChange={(e) => setFormData({ ...formData, volume: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div className="md:col-span-2 flex gap-2">
              <button
                type="submit"
                className="bg-green-600 hover:bg-green-700 text-white font-semibold py-2 px-4 rounded-lg transition duration-300"
              >
                {editingStock ? 'Update' : 'Create'}
              </button>
              <button
                type="button"
                onClick={resetForm}
                className="bg-gray-500 hover:bg-gray-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-300"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Company ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Open</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">High</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Low</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Close</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Volume</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {stocks.map((stock) => (
                <tr key={stock.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{stock.companyId || 'N/A'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{stock.date}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${stock.openValue.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-green-600">${stock.highValue.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-red-600">${stock.lowValue.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${stock.closeValue.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{stock.volume.toLocaleString()}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => handleEdit(stock)}
                      className="text-yellow-600 hover:text-yellow-900 mr-3"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(stock.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {stocks.length === 0 && !loading && (
        <div className="text-center text-gray-500 mt-8">
          No stocks found. Add your first stock quotation!
        </div>
      )}
    </div>
  );
};

export default StockList;
