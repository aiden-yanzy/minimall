// 分 -> 元 字符串（保留必要小数）
export function fen2yuan(fen) {
  if (fen === null || fen === undefined) return '0.00'
  const y = fen / 100
  return Number.isInteger(y) ? String(y) : y.toFixed(2)
}

// 分 -> 元 数字
export function fen2yuanNum(fen) {
  return (fen || 0) / 100
}
