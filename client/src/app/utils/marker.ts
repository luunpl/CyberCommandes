import { circleMarker, CircleMarker, divIcon, marker, Marker } from 'leaflet';

export function getCircleMarker(latlng: [number, number], color: string): CircleMarker {
    return circleMarker(latlng, {
        radius: 6,
        fillColor: color,
        color: '#fff',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
    }).bindPopup(`Point: ${latlng[0].toFixed(4)}, ${latlng[1].toFixed(4)}`);
}

export function getDepotMarker(latlng: [number, number]): Marker {
    const icon = divIcon({
        html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 36" width="16" height="24">
          <path d="M12 0C5.372 0 0 5.372 0 12c0 9 12 24 12 24s12-15 12-24C24 5.372 18.628 0 12 0z" fill="#EA4335"/>
          <circle cx="12" cy="12" r="5" fill="white"/>
        </svg>`,
        className: '',
        iconSize: [16, 24],
        iconAnchor: [8, 24],
        popupAnchor: [0, -24]
    });
    return marker(latlng, { icon }).bindPopup('Entrepôt');
}