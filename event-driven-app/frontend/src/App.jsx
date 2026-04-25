import { useState, useEffect } from 'react'
import './index.css'

const MENU_ITEMS = [
  { id: 1, name: 'Burger Maison', price: 12.5, icon: '🍔' },
  { id: 2, name: 'Pizza Margherita', price: 14.0, icon: '🍕' },
  { id: 3, name: 'Sushi Mix', price: 22.0, icon: '🍣' },
  { id: 4, name: 'Plat du Jour', price: 18.0, icon: '🥘', desc: '(Stock limité !)' },
  { id: 5, name: 'Tiramisu', price: 6.0, icon: '🍰' },
  { id: 6, name: 'Soda Frais', price: 3.5, icon: '🥤' }
];

function App() {
  const [cart, setCart] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchEvents = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/events');
      if (res.ok) setEvents(await res.json());
    } catch (e) { }
  };

  useEffect(() => {
    fetchEvents();
    const interval = setInterval(fetchEvents, 1000);
    return () => clearInterval(interval);
  }, []);

  const addToCart = (item) => setCart([...cart, item]);

  const removeFromCart = (index) => {
    const newCart = [...cart];
    newCart.splice(index, 1);
    setCart(newCart);
  };

  const cartTotal = cart.reduce((sum, item) => sum + item.price, 0);

  const checkout = async () => {
    if (cart.length === 0) return;
    setLoading(true);
    try {
      const platQty = cart.filter(i => i.name === 'Plat du Jour').length;
      await fetch(`http://localhost:8080/api/checkout?total=${cartTotal}&platDuJourQty=${platQty}`, { method: 'POST' });
      setCart([]);
      fetchEvents();
    } catch (e) {
      alert("Erreur de connexion avec l'API Java.");
    }
    setLoading(false);
  };

  // Grouper les événements par orderId
  const orders = {};
  let totalLoyaltyPoints = 0;

  events.forEach(e => {
    if (e.topic === 'loyalty.points_awarded') {
      totalLoyaltyPoints += 5;
    }

    if (!e.orderId) return;
    if (!orders[e.orderId]) {
      orders[e.orderId] = { id: e.orderId, steps: [], failed: false, outOfStock: false };
    }
    orders[e.orderId].steps.push(e.topic);
    if (e.topic === 'payment.failed') orders[e.orderId].failed = true;
    if (e.topic === 'order.out_of_stock') orders[e.orderId].outOfStock = true;
  });

  // Trier les commandes (la plus récente en haut)
  const sortedOrders = Object.values(orders).reverse();

  return (
    <div className="app-container">
      <header>
        <div>
          <h1>🍔 <span>Fast</span>Delivery</h1>
          <div style={{ color: 'var(--text-secondary)' }}>
            Propulsé par Java Event-Driven Architecture
          </div>
        </div>
        <div className="glass" style={{ padding: '0.75rem 1.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Points de Fidélité</div>
          <div style={{ fontSize: '1.5rem', fontWeight: '800', color: 'var(--accent-color)' }}>🎁 {totalLoyaltyPoints} pts</div>
        </div>
      </header>

      <div className="main-content">
        <div className="left-panel">
          <div className="glass menu-section" style={{ marginBottom: '2rem' }}>
            <h2>Menu</h2>
            <div className="menu-grid">
              {MENU_ITEMS.map(item => (
                <div key={item.id} className="glass menu-item" onClick={() => addToCart(item)}>
                  <div className="menu-icon">{item.icon}</div>
                  <div className="menu-name">{item.name} {item.desc && <span style={{ fontSize: '0.8rem', color: 'var(--danger-color)' }}><br />{item.desc}</span>}</div>
                  <div className="menu-price">{item.price.toFixed(2)} €</div>
                </div>
              ))}
            </div>
          </div>

          <div className="orders-tracker">
            <h2>Suivi de Commandes (En Direct)</h2>
            {sortedOrders.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>Aucune commande en cours.</p>
            ) : (
              sortedOrders.map(order => {
                const hasPlaced = order.steps.includes('order.placed');
                const hasReserved = order.steps.includes('inventory.reserved');
                const hasPaid = order.steps.includes('payment.done');
                const hasShipped = order.steps.includes('order.shipped');
                const hasDelivered = order.steps.includes('delivery.completed');

                let statusText = "En attente...";
                let cardClass = "glass order-card";

                if (order.failed) { statusText = "Paiement Refusé ❌"; cardClass += " failed"; }
                else if (order.outOfStock) { statusText = "Rupture de Stock ⚠️"; cardClass += " failed"; }
                else if (hasDelivered) { statusText = "Livrée ✅"; cardClass += " delivered"; }
                else if (hasShipped) { statusText = "En route 🛵"; }
                else if (hasPaid) { statusText = "Paiement validé 💳"; }
                else if (hasReserved) { statusText = "Stock validé 📦"; }
                else if (hasPlaced) { statusText = "Commande Reçue 📝"; }

                return (
                  <div key={order.id} className={cardClass}>
                    <div className="order-header">
                      <span className="order-id">#{order.id.split('_')[1]}</span>
                      <span className="order-status" style={{
                        color: (order.failed || order.outOfStock) ? 'var(--danger-color)' : (hasDelivered ? 'var(--success-color)' : 'var(--accent-color)')
                      }}>{statusText}</span>
                    </div>

                    {!(order.failed && !order.outOfStock) && (
                      <div className="status-steps" style={{ marginTop: '1rem', overflowX: 'auto', paddingBottom: '1rem' }}>
                        <div className={`step ${hasPlaced ? 'active' : ''}`}>
                          <div className="step-icon">📝</div>
                          <div className="step-label">Reçue</div>
                        </div>
                        <div className={`step ${order.outOfStock ? 'active failed-step' : (hasReserved ? 'active' : '')}`}>
                          <div className="step-icon">{order.outOfStock ? '❌' : '📦'}</div>
                          <div className="step-label" style={{ color: order.outOfStock ? 'var(--danger-color)' : 'inherit' }}>
                            {order.outOfStock ? 'Épuisé' : 'Stock'}
                          </div>
                        </div>
                        <div className={`step ${hasPaid ? 'active' : ''}`} style={{ opacity: order.outOfStock ? 0.1 : undefined }}>
                          <div className="step-icon">💳</div>
                          <div className="step-label">Payée</div>
                        </div>
                        <div className={`step ${hasShipped ? 'active' : ''}`} style={{ opacity: order.outOfStock ? 0.1 : undefined }}>
                          <div className="step-icon">🛵</div>
                          <div className="step-label">En route</div>
                        </div>
                        <div className={`step ${hasDelivered ? 'active' : ''}`} style={{ opacity: order.outOfStock ? 0.1 : undefined }}>
                          <div className="step-icon">✅</div>
                          <div className="step-label">Livrée</div>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>

        <div className="right-panel">
          <div className="glass cart-section">
            <h2>Votre Panier</h2>
            <div className="cart-items">
              {cart.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)', textAlign: 'center', marginTop: '2rem' }}>Le panier est vide.</p>
              ) : (
                cart.map((item, idx) => (
                  <div key={idx} className="cart-item">
                    <span>{item.icon} {item.name}</span>
                    <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                      <span>{item.price.toFixed(2)} €</span>
                      <button
                        onClick={() => removeFromCart(idx)}
                        style={{ background: 'transparent', border: 'none', color: 'var(--danger-color)', cursor: 'pointer', fontSize: '1rem', padding: '0 5px' }}
                        title="Retirer"
                      >❌</button>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="cart-total">
              <span>Total</span>
              <span>{cartTotal.toFixed(2)} €</span>
            </div>

            <button
              className="checkout-btn"
              disabled={cart.length === 0 || loading}
              onClick={checkout}
            >
              {loading ? 'Traitement...' : 'Commander'}
            </button>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textAlign: 'center', marginTop: '1rem' }}>
              *Les commandes &gt; 1000€ simuleront un paiement refusé.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
